package middle.negatives;

import Requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import Tests.BaseTest;
import generators.*;
import io.restassured.common.mapper.TypeRef;
import models.*;
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

        NewUserResponse newUserResponse1 = new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser1).extract().as(NewUserResponse.class);

        NewUserResponse newUserResponse2 = new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser2).extract().as(NewUserResponse.class);

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
        List<GetCustomerAccountResponse> accountsDebetResponse = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.isOk())
                .get()
                .extract().as(new TypeRef<List<GetCustomerAccountResponse>>() {});
        List<GetCustomerAccountResponse> accountsDebet = accountsDebetResponse.stream().toList();



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
        List<GetCustomerAccountResponse> accountsCreditResponse = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.isOk())
                .get()
                .extract()
                .as(new TypeRef<List<GetCustomerAccountResponse>>() {});
        List<GetCustomerAccountResponse> accountsCredit = accountsCreditResponse.stream().toList();

        //среди счетов найдем тот, куда слали перевод и запишем его в переменную
        GetCustomerAccountResponse targetCredAcc = accountsCredit.stream()
                .filter(a -> accountCreditId.equals(a.getId())) //номер счета
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //проверки первого уровня. Убедимся, что текущий баланс = 0 потому что перевода не было
        soflty.assertThat(targetCredAcc.getBalance()).isEqualTo(0.0F);

//Проверим, что транзакций нет, т.к не было пополнения и не было перевода
        soflty.assertThat(targetCredAcc.getTransactions()).isEmpty();

//удалим только тех юзеров, что создали в тесте
        //поместим в лист ответы на запросы о создании юзеров
        List<NewUserResponse> createdUsers = new ArrayList<>(List.of(newUserResponse1,newUserResponse2));

        //удалим их. И сразу же вызовем getAllUsers от админа, чтобы проверить, что id не существует
        for(NewUserResponse user: createdUsers){
            String successMessage = new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk())
                    .delete(new DeleteByUserId(user.getId()))
                    .extract().asString();
            String expected = String.format("User with ID %d deleted successfully.", user.getId());
            soflty.assertThat(successMessage).isEqualTo(expected);

            //Убедимся, что удаленного юзера реально нет. Админ вызывает getAllUsers, сразу положим в лист
            List<GetAllUsersResponse> users = new GetAllUsersRequester(
                    RequestSpecs.adminAuth(),
                    ResponseSpecs.isOk()
            ).get().extract().as(new TypeRef<List<GetAllUsersResponse>>() {});

            //вытащим их id в лист
            List<Integer> userIds = users.stream()
                    .map(GetAllUsersResponse::getId)
                    .toList();

            //проверим, что удаленного юзера реально нет
            soflty.assertThat(userIds)
                    .as("удалённый id не должен быть среди пользователей")
                    .doesNotContain(user.getId());
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

        NewUserResponse newUserResponse1 = new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser1).extract().as(NewUserResponse.class);

        NewUserResponse newUserResponse2 = new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser2).extract().as(NewUserResponse.class);

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
        List<GetCustomerAccountResponse> accountsDebetResponse = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.isOk())
                .get()
                .extract()
                .as(new TypeRef<List<GetCustomerAccountResponse>>() {});

        List<GetCustomerAccountResponse> accountsDebet = accountsDebetResponse.stream().toList();


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
        List<GetCustomerAccountResponse> accountsCreditResponse = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.isOk())
                .get()
                .extract().as(new TypeRef<List<GetCustomerAccountResponse>>() {});
        List<GetCustomerAccountResponse> accountsCredit = accountsCreditResponse.stream().toList();


        //среди счетов найдем тот, куда слали перевод и запишем его в переменную
        GetCustomerAccountResponse targetCredAcc = accountsCredit.stream()
                .filter(a -> accountCreditId.equals(a.getId())) //номер счета
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //проверки первого уровня. Убедимся, что текущий баланс = 0 потому что перевода не было
        soflty.assertThat(targetCredAcc.getBalance()).isEqualTo(0.0F);

//Проверим, что транзакций нет, т.к не было пополнения и не было перевода
        soflty.assertThat(targetCredAcc.getTransactions()).isEmpty();


        //удалим только тех юзеров, что создали в тесте
        //поместим в лист ответы на запросы о создании юзеров
        List<NewUserResponse> createdUsers = new ArrayList<>(List.of(newUserResponse1,newUserResponse2));

        //удалим их. И сразу же вызовем getAllUsers от админа, чтобы проверить, что id не существует
        for(NewUserResponse user: createdUsers){
            String successMessage = new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk())
                    .delete(new DeleteByUserId(user.getId()))
                    .extract().asString();
            String expected = String.format("User with ID %d deleted successfully.", user.getId());
            soflty.assertThat(successMessage).isEqualTo(expected);

            //Убедимся, что удаленного юзера реально нет. Админ вызывает getAllUsers, сразу положим в лист
            List<GetAllUsersResponse> users = new GetAllUsersRequester(
                    RequestSpecs.adminAuth(),
                    ResponseSpecs.isOk()
            ).get().extract().as(new TypeRef<List<GetAllUsersResponse>>() {});

            //вытащим их id в лист
            List<Integer> userIds = users.stream()
                    .map(GetAllUsersResponse::getId)
                    .toList();

            //проверим, что удаленного юзера реально нет
            soflty.assertThat(userIds)
                    .as("удалённый id не должен быть среди пользователей")
                    .doesNotContain(user.getId());
        }
    }

}
