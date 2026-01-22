package senior.negatives;


import api.generators.ErrorMessage;
import api.generators.TransactionType;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import Tests.BaseTest;
import api.models.NewUserRequest;
import api.models.GetCustomerAccountResponse;
import api.models.TransferMoneyRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UserTransferInvalidSumNegativeTest extends BaseTest {
 /*

Тест-кейсы этого файла:

1. Юзер переводит 0 (при балансе 5000)
2. Юзер переводит -1 (при балансе 5000)
3. Юзер переводит 10 001 (при балансе 5000)
4. Юзер переводит 6000 (при балансе в 5000)
5. Юзер переводит сумму 10 001 при балансе больше 10 001

*/


    public static Stream<Arguments> invalidSumToTransfer() {
        return Stream.of(
                Arguments.of(0.0, ErrorMessage.TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001.getMessage()),
                Arguments.of(-1.0, ErrorMessage.TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001.getMessage()),
                Arguments.of(10001.0, ErrorMessage.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage()),
                Arguments.of(6000.0, ErrorMessage.INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNT.getMessage())
        );
    }
/*
1. Юзер переводит 0 (при балансе 5000)
2. Юзер переводит -1 (при балансе 5000)
3. Юзер переводит 10 001 (при балансе 5000)
4. Юзер переводит 6000 (при балансе в 5000)
 */

    @ParameterizedTest
    @MethodSource("invalidSumToTransfer")
    public void userTransferInvalidSumToUser(Double invalidSum, String expectedErrorMessage) {

        //cоздать 2 юзеров
        List<NewUserRequest> users = new ArrayList<>();
        for (int i = 0; i <= 1; i++) {
            NewUserRequest newUser = AdminSteps.createUser();
            users.add(newUser);
        }
        NewUserRequest user1 = users.get(0);
        NewUserRequest user2 = users.get(1);

        //создадим счета юзерам
        Long user1Id = UserSteps.createsAccount(user1).getId();
        Long user2Id = UserSteps.createsAccount(user2).getId();

        //пополним счет юзеру1
        UserSteps.makesDeposit(user1Id, user1);

        //Запросим информацию о счетах после пополнения, чтобы зафиксировать их балансы до транзакции
        List<GetCustomerAccountResponse> user1AllAccsBefore = UserSteps.getsAccounts(user1);
        //найдем счет-дебет
        GetCustomerAccountResponse user1Before = user1AllAccsBefore.stream()
                .filter(a -> a.getId().equals(user1Id))
                .findAny()
                .orElseThrow(() -> new AssertionError("счета не существует"));

        //зафиксируем его баланс до перевода
        Double balanceUser1BeforeTransfer = user1Before.getBalance();

        //найдем счет-кредит
        List<GetCustomerAccountResponse> user2AllAccsBefore = UserSteps.getsAccounts(user2);
        GetCustomerAccountResponse creditBefore = user2AllAccsBefore.stream()
                .filter(a -> a.getId().equals(user2Id))
                .findAny()
                .orElseThrow(() -> new AssertionError("счета не существует"));
        //зафиксируем его баланс до перевода
        Double balanceUser2BeforeTransfer = creditBefore.getBalance();

        //перевод
        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(user1Id)
                .amount(invalidSum)
                .receiverAccountId(user2Id)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(transferMoney).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);

        //Проверка 1. У Юзера1 не списались деньги, баланс такой же и нет транзакции трансфер аут
        List<GetCustomerAccountResponse> user1AllAccsAfter = UserSteps.getsAccounts(user1);

        GetCustomerAccountResponse targetUser1Acc = user1AllAccsAfter.stream()
                .filter(a -> a.getId().equals(user1Id)) //номер счета
                .filter(a -> a.getBalance().equals(balanceUser1BeforeTransfer)) //баланс такой же
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт отправителя не найден"));

        //нет транзакции трансфер-аут
        boolean hasNoTransferOut = targetUser1Acc.getTransactions().stream()
                .noneMatch(t ->
                        TransactionType.TRANSFER_OUT.getMessage().equals(t.getType())
                );
        soflty.assertThat(hasNoTransferOut)
                .as("TRANSFER_OUT транзакций быть не должно")
                .isTrue();


        //Проверка 2. У Юзера 2 не появились деньги, баланс не изменился и транзакции трансфер ин нет
        List<GetCustomerAccountResponse> user2AllAccsAfter = UserSteps.getsAccounts(user2);
        GetCustomerAccountResponse targetUser2Acc = user2AllAccsAfter.stream()
                .filter(a -> a.getId().equals(user2Id)) //номер счета
                .filter(a -> a.getBalance().equals(balanceUser2BeforeTransfer)) //баланс как и был
                .findAny()
                .orElseThrow(() -> new AssertionError("Счёт получателя не найден"));
        //нет транзакции трансфер-ин
        boolean hasNoTransferIn = targetUser2Acc.getTransactions().stream()
                .noneMatch(t ->
                        TransactionType.TRANSFER_IN.getMessage().equals(t.getType())
                );
        soflty.assertThat(hasNoTransferIn)
                .as("TRANSFER_IN транзакций быть не должно")
                .isTrue();

        //удалить пользователей
        for (NewUserRequest u : users) {
            AdminSteps.deletesUser(u);
        }
    }


    // 5. Юзер переводит сумму 10 001 при балансе больше 10 001
    public static Stream<Arguments> insufficientSumToTransfer() {
        return Stream.of(
                Arguments.of(10001.0, ErrorMessage.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage())
        );
    }

    @ParameterizedTest
    @MethodSource("insufficientSumToTransfer")
    public void userTransferInsufficientSumToUser(Double invalidSum, String expectedErrorMessage) {
        //cоздать 2 юзеров
        List<NewUserRequest> users = new ArrayList<>();
        for (int i = 0; i <= 1; i++) {
            NewUserRequest newUser = AdminSteps.createUser();
            users.add(newUser);
        }
        NewUserRequest user1 = users.get(0);
        NewUserRequest user2 = users.get(1);

        //создадим счета юзерам
        Long user1Id = UserSteps.createsAccount(user1).getId();
        Long user2Id = UserSteps.createsAccount(user2).getId();

        //пополним счет дважды, сумма 10 000
        UserSteps.makesDepositX2(user1Id, user1);
        //и еще на 5000
        UserSteps.makesDeposit(user1Id, user1);

        //Запросим информацию о счетах после пополнения, чтобы зафиксировать их балансы до транзакции
        List<GetCustomerAccountResponse> user1AllAccsBefore = UserSteps.getsAccounts(user1);
        //найдем счет-дебет
        GetCustomerAccountResponse user1Before = user1AllAccsBefore.stream()
                .filter(a -> a.getId().equals(user1Id))
                .findAny()
                .orElseThrow(() -> new AssertionError("счета не существует"));

        //зафиксируем его баланс до перевода
        Double balanceUser1BeforeTransfer = user1Before.getBalance();

        //найдем счет-кредит
        List<GetCustomerAccountResponse> user2AllAccsBefore = UserSteps.getsAccounts(user2);
        GetCustomerAccountResponse creditBefore = user2AllAccsBefore.stream()
                .filter(a -> a.getId().equals(user2Id))
                .findAny()
                .orElseThrow(() -> new AssertionError("счета не существует"));
        //зафиксируем его баланс до перевода
        Double balanceUser2BeforeTransfer = creditBefore.getBalance();


        //перевод
        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(user1Id)
                .amount(invalidSum)
                .receiverAccountId(user2Id)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(transferMoney).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);

        //Проверка 1. У Юзера1 не списались деньги, баланс такой же и нет транзакции трансфер аут
        List<GetCustomerAccountResponse> user1AllAccsAfter = UserSteps.getsAccounts(user1);

        GetCustomerAccountResponse targetUser1Acc = user1AllAccsAfter.stream()
                .filter(a -> a.getId().equals(user1Id)) //номер счета
                .filter(a -> a.getBalance().equals(balanceUser1BeforeTransfer)) //баланс такой же
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт отправителя не найден"));

        //нет транзакции трансфер-аут
        boolean hasNoTransferOut = targetUser1Acc.getTransactions().stream()
                .noneMatch(t ->
                        TransactionType.TRANSFER_OUT.getMessage().equals(t.getType())
                );
        soflty.assertThat(hasNoTransferOut)
                .as("TRANSFER_OUT транзакций быть не должно")
                .isTrue();


        //Проверка 2. У Юзера 2 не появились деньги, баланс не изменился и транзакции трансфер ин нет
        List<GetCustomerAccountResponse> user2AllAccsAfter = UserSteps.getsAccounts(user2);
        GetCustomerAccountResponse targetUser2Acc = user2AllAccsAfter.stream()
                .filter(a -> a.getId().equals(user2Id)) //номер счета
                .filter(a -> a.getBalance().equals(0.0)) //баланс ноль
                .findAny()
                .orElseThrow(() -> new AssertionError("Счёт получателя не найден"));
        //нет транзакции трансфер-ин
        boolean hasNoTransferIn = targetUser2Acc.getTransactions().stream()
                .noneMatch(t ->
                        TransactionType.TRANSFER_IN.getMessage().equals(t.getType())
                );
        soflty.assertThat(hasNoTransferIn)
                .as("TRANSFER_IN транзакций быть не должно")
                .isTrue();

        //удалить пользователей
        for (NewUserRequest u : users) {
            AdminSteps.deletesUser(u);
        }
    }
}

