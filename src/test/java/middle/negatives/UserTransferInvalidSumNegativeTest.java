package Tests.negatives;

import Requests.*;
import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import Tests.BaseTest;
import generators.*;
import models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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



    public static Stream<Arguments> invalidSumToTransfer(){
        return Stream.of(
                Arguments.of(0.0F, ErrorMessage.TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001.getMessage()),
                Arguments.of(-1.0F, ErrorMessage.TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001.getMessage()),
                Arguments.of(10001.0F, ErrorMessage.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage()),
                Arguments.of(6000.0F, ErrorMessage.INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNT.getMessage())
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
    public void userTransferInvalidSumToUser(float invalidSum, String expectedErrorMessage) {


        //cоздан юзер 1. Он будет дебетом
        NewUserRequest newUser1 = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        //юзер 2, будет кредитом
        NewUserRequest newUser2 = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser1);

        new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser2);

//создаем счет юзеру 1 и записываем его в переменную.
        CreateAnAccResponse createAnAccResponse1 = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract().as(CreateAnAccResponse.class);
        Integer accountDebetId = createAnAccResponse1.getId();
        float balanceDebet = createAnAccResponse1.getBalance();


//создаем счет юзеру 2. Вытащим из него баланс для дальнейших проверок и номер счета
        CreateAnAccResponse createAnAccResponse2 = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract().as(CreateAnAccResponse.class);

        Integer accountCreditId = createAnAccResponse2.getId();
        float balanceCredit = createAnAccResponse2.getBalance();

        //пополнение cчета у юзера 1. Через енум подтянули максимальную сумму для депозита.
        MakeDepositResponse makeDepositResponse = new MakeDepositRequester
                (RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()), ResponseSpecs.isOk())
                .post(new MakeDeposit
                        .Builder()
                        .setBalance(MaxSumsForDepositAndTransactions.DEPOSIT.getValue())
                        .setId(accountDebetId)
                        .build()
                ).extract().as(MakeDepositResponse.class);
        balanceDebet = makeDepositResponse.getBalance();


        //перевод
        String actualErrorMessage = new TransferMoneyRequester(RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.BadRequest())
                .post(new TransferMoney
                        .Builder()
                        .amount(invalidSum)
                        .senderAccountId(accountDebetId)
                        .receiverAccountId(accountCreditId)
                        .build()).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);

        //!!!!Проверки
        //Проверка 1. Убедимся, что у юзера-дебета не убавилось денег на счету
        // у дебета остался такой же остаток как и был
        //есть счет который фигурировал в переводе как дебет
        //у этого счета НЕТ транзакций transfer out

        //приходит массив счетов дебета
        List<GetCustomerAccountResponse> accountsDebet = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.isOk())
                .get().extract()
                .jsonPath()
                .getList("", GetCustomerAccountResponse.class);

        //среди счетов найдем тот, куда слали перевод и запишем его в переменную
        GetCustomerAccountResponse targetDebAcc = accountsDebet.stream()
                .filter(a -> accountDebetId.equals(a.getId())) //номер счета
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //проверки первого уровня. Убедимся, что текущий баланс = 5000 (тк перевод не совершился)
        soflty.assertThat(targetDebAcc.getBalance()).isEqualTo(MaxSumsForDepositAndTransactions.DEPOSIT.getValue());

        //теперь смотрим его транзакции. Не должно быть транзакции transfer_out
        GetCustomerAccountResponse.Transaction targetDebetTransaction = targetDebAcc.getTransactions().stream()
                .filter(t -> !t.getType().equals(TransactionType.TRANSFER_OUT.getMessage()) //что нет транзакций transfer_out
                        && t.getAmount() == MaxSumsForDepositAndTransactions.DEPOSIT.getValue()
                )
                .findAny()
                .orElseThrow(() -> new AssertionError("Существует транзакция transfer_out"));

        //Проверка 2
        //Убедимся, что у кредита не изменился баланс
        //приходит массив счетов кредита
        List<GetCustomerAccountResponse> accountsCredit = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.isOk())
                .get().extract()
                .jsonPath()
                .getList("", GetCustomerAccountResponse.class);

        //среди счетов найдем тот, куда слали перевод и запишем его в переменную
        GetCustomerAccountResponse targetCredAcc = accountsCredit.stream()
                .filter(a -> accountCreditId.equals(a.getId())) //номер счета
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //проверки первого уровня. Убедимся, что текущий баланс = 0 потому что перевода не было
        soflty.assertThat(targetCredAcc.getBalance()).isEqualTo(0.0F);

//Проверим, что транзакций нет, т.к не было пополнения и не было перевода
        soflty.assertThat(targetCredAcc.getTransactions()).isEmpty();


        //удалим всех
        List<Integer> userIds = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().jsonPath().getList("id",  Integer.class);

        for (Integer userId : userIds){
            new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk()).delete(new DeleteByUserId(userId));

        }


    }
    /*
   5. Юзер переводит сумму 10 001 при балансе больше 10 001
    */
    public static Stream<Arguments> insufficientSumToTransfer(){
        return Stream.of(
                Arguments.of(10001.0F, ErrorMessage.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage())
        );
    }
    @ParameterizedTest
    @MethodSource("insufficientSumToTransfer")
    public void userTransferInsufficientSumToUser(float invalidSum, String expectedErrorMessage) {

        //cоздан юзер 1. Он будет дебетом
        NewUserRequest newUser1 = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        //юзер 2, будет кредитом
        NewUserRequest newUser2 = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser1);

        new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser2);

//создаем счет юзеру 1 и записываем его в переменную.
        CreateAnAccResponse createAnAccResponse1 = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract().as(CreateAnAccResponse.class);
        Integer accountDebetId = createAnAccResponse1.getId();
        float balanceDebet = createAnAccResponse1.getBalance();


//создаем счет юзеру 2. Вытащим из него баланс для дальнейших проверок и номер счета
        CreateAnAccResponse createAnAccResponse2 = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract().as(CreateAnAccResponse.class);

        Integer accountCreditId = createAnAccResponse2.getId();
        float balanceCredit = createAnAccResponse2.getBalance();

        //пополнение cчета у юзера 1. Через енум подтянули максимальную сумму для депозита. Пополним трижды чтобы смочь перевести 10 001
        for (int i = 1; i <= 3; i++) {
            MakeDepositResponse makeDepositResponse = new MakeDepositRequester
                    (RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()), ResponseSpecs.isOk())
                    .post(new MakeDeposit
                            .Builder()
                            .setBalance(MaxSumsForDepositAndTransactions.DEPOSIT.getValue())
                            .setId(accountDebetId)
                            .build()
                    ).extract().as(MakeDepositResponse.class);
            balanceDebet = makeDepositResponse.getBalance();
        }


        //перевод
        String actualErrorMessage = new TransferMoneyRequester(RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.BadRequest())
                .post(new TransferMoney
                        .Builder()
                        .amount(invalidSum)
                        .senderAccountId(accountDebetId)
                        .receiverAccountId(accountCreditId)
                        .build()).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);
        //!!!!Проверки
        //Проверка 1. Убедимся, что у юзера-дебета не убавилось денег на счету
        // у дебета остался такой же остаток как и был
        //есть счет который фигурировал в переводе как дебет
        //у этого счета НЕТ транзакций transfer out

        //приходит массив счетов дебета
        List<GetCustomerAccountResponse> accountsDebet = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.isOk())
                .get().extract()
                .jsonPath()
                .getList("", GetCustomerAccountResponse.class);

        //среди счетов найдем тот, куда слали перевод и запишем его в переменную
        GetCustomerAccountResponse targetDebAcc = accountsDebet.stream()
                .filter(a -> accountDebetId.equals(a.getId())) //номер счета
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //проверки первого уровня. Убедимся, что текущий баланс = 15000 (тк перевод не совершился)
        soflty.assertThat(targetDebAcc.getBalance()).isEqualTo(3 * MaxSumsForDepositAndTransactions.DEPOSIT.getValue());

        //теперь смотрим его транзакции. Не должно быть транзакции transfer_out
        GetCustomerAccountResponse.Transaction targetDebetTransaction = targetDebAcc.getTransactions().stream()
                .filter(t -> !t.getType().equals(TransactionType.TRANSFER_OUT.getMessage()) //что нет транзакций transfer_out
                        && t.getAmount() == MaxSumsForDepositAndTransactions.DEPOSIT.getValue()
                )
                .findAny()
                .orElseThrow(() -> new AssertionError("Существует транзакция transfer_out"));

        //Проверка 2
        //Убедимся, что у кредита не изменился баланс
        //приходит массив счетов кредита
        List<GetCustomerAccountResponse> accountsCredit = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.isOk())
                .get().extract()
                .jsonPath()
                .getList("", GetCustomerAccountResponse.class);

        //среди счетов найдем тот, куда слали перевод и запишем его в переменную
        GetCustomerAccountResponse targetCredAcc = accountsCredit.stream()
                .filter(a -> accountCreditId.equals(a.getId())) //номер счета
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //проверки первого уровня. Убедимся, что текущий баланс = 0 потому что перевода не было
        soflty.assertThat(targetCredAcc.getBalance()).isEqualTo(0.0F);

//Проверим, что транзакций нет, т.к не было пополнения и не было перевода
        soflty.assertThat(targetCredAcc.getTransactions()).isEmpty();


        //удалим всех
        List<Integer> userIds = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().jsonPath().getList("id",  Integer.class);

        for (Integer userId : userIds){
            new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk()).delete(new DeleteByUserId(userId));

        }
    }

}
