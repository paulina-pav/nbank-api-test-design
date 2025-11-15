package middle.positive;

import Requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import Tests.BaseTest;
import generators.*;
import io.restassured.common.mapper.TypeRef;
import models.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class UserTransferMoneyTest extends BaseTest {
    /*
Тест-кейсы этого файла:
1. Юзер успешно переводит деньги на существующий счет другого юзера
2. Юзер успешно переводит деньги с одного своего счета на другой

     */

    @Test
   // 1. Юзер успешно переводит деньги на существующий счет другого юзера
    public void userTransfersMoneyToUser() {
        //Шаг 1: создаем юзеров
        //юзер 1 - дебет
        NewUserRequest newUser1 = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();
        //юзер 2 - кредит
        NewUserRequest newUser2 = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        NewUserResponse newUserResponse1 = new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser1).extract().as(NewUserResponse.class);

        NewUserResponse newUserResponse2 = new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser2).extract().as(NewUserResponse.class);

//Предусловие шаг 2: создаем счет юзеру 1
        Integer accountDebetId = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract()
                .path("id");


//Предусловие шаг 3: создаем счет юзеру 2
        CreateAnAccResponse createAnAccResponse2 = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract().as(CreateAnAccResponse.class);

        Integer accountCreditId = createAnAccResponse2.getId();
        float balanceCredit = createAnAccResponse2.getBalance();


        //Предусловие шаг 4: пополнение cчета у юзера 1.
        //сделаем депозит дважды. Чтобы потом проверить перевод на максимальную сумму - 10 000

        float balanceDebet = 0.0F;//создадим переменную для записи баланса счета дебета, чтобы потом считать баланс после перевода
        for (int i = 1; i <= 2; i++) {
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


//Шаг 5: юзер 1 переводит юзеру 2
        TransferMoneyResponse transferMoneyResponse = new TransferMoneyRequester(RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.isOk())
                .post(new TransferMoney
                        .Builder()
                        .amount(MaxSumsForDepositAndTransactions.TRANSACTION.getValue())
                        .senderAccountId(accountDebetId)
                        .receiverAccountId(accountCreditId)
                        .build()).extract().as(TransferMoneyResponse.class);

        soflty.assertThat(transferMoneyResponse.getSenderAccountId()).isEqualTo(accountDebetId);
        soflty.assertThat(transferMoneyResponse.getReceiverAccountId()).isEqualTo(accountCreditId);
        soflty.assertThat(transferMoneyResponse.getAmount()).isEqualTo(MaxSumsForDepositAndTransactions.TRANSACTION.getValue());
        soflty.assertThat(transferMoneyResponse.getMessage()).isEqualTo(ServiceMessages.SUCCESSFUL_TRANSFER.getMessage());


        //Проверка 1. ОТПРАВИТЕЛЬ:
        // GetCustomerAccount
        // проверим, что:
        // у дебета убавился общий баланс на сумму перевода
        //есть счет который фигурировал в переводе как дебет
        //у этого счета есть транзакция transfer out на сумму 5000

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

        //проверки первого уровня. Убедимся, что текущий баланс = начальный баланс -5000
        soflty.assertThat(targetDebAcc.getBalance()).isEqualTo(balanceDebet - MaxSumsForDepositAndTransactions.TRANSACTION.getValue());

        //теперь смотрим его транзакции и ищем ту где есть нужные совпадения.
        GetCustomerAccountResponse.Transaction targetDebetTransaction = targetDebAcc.getTransactions().stream()
                .filter(t -> t.getAmount() == MaxSumsForDepositAndTransactions.TRANSACTION.getValue()) //сумма перевода
                .filter(t -> t.getType().equals(TransactionType.TRANSFER_OUT.getMessage()))
                .filter(t -> t.getRelatedAccountId().equals(accountCreditId)) //вставить сюда счет кредита куда слали
                .findAny()
                .orElseThrow(() -> new AssertionError("Нужная транзакция не найдена"));


        //ПРОВЕРКА 2: получатель
        //приходит массив счетов кредита
        List<GetCustomerAccountResponse> accountsСreditResponse = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.isOk())
                .get()
                .extract().as(new TypeRef<List<GetCustomerAccountResponse>>() {});
        List<GetCustomerAccountResponse> accountsСredit = accountsСreditResponse.stream().toList();

        //среди счетов найдем тот, куда слали перевод и запишем его в переменную
        GetCustomerAccountResponse targetCredAcc = accountsСredit.stream()
                .filter(a -> accountCreditId.equals(a.getId())) //номер счета
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //проверки первого уровня. Убедимся, что текущий баланс = начальный баланс +5000
        soflty.assertThat(targetCredAcc.getBalance()).isEqualTo(balanceCredit + MaxSumsForDepositAndTransactions.TRANSACTION.getValue());


        //Нашли счет. Теперь смотрим есть ли в нем нужные транзакции
        GetCustomerAccountResponse.Transaction targetCreditTransaction = targetCredAcc.getTransactions().stream()
                .filter(t -> t.getAmount() == MaxSumsForDepositAndTransactions.TRANSACTION.getValue()) //сумма перевода
                .filter(t -> t.getType().equals(TransactionType.TRANSFER_IN.getMessage()))
                .filter(t -> t.getRelatedAccountId().equals(accountDebetId)) //вставить сюда счет детеба откуда слали
                .findAny()
                .orElseThrow(() -> new AssertionError("Нужная транзакция не найдена"));



        //Шаг 6: удалим только тех юзеров, что создали в тесте
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

    //Тест 2: юзер переводит средства между своими счетами
    @Test
    public void userTransfersMoneyBetweenItsAccounts(){
        //Предусловие шаг 1: создаем юзера
        NewUserRequest newUser1 = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        NewUserResponse newUserResponse1 = new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser1).extract().as(NewUserResponse.class);

        //Предусловие шаг 2: создадим счет, который будет дебетом и запишем его в переменную
        Integer accountDebetId = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract()
                .path("id");

        //Предусловие шаг 3: создаем второй счет, который будет кредитом. Выпишем id счета и баланс для дальнейших сравнений
        CreateAnAccResponse createAnAccDebetResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract().as(CreateAnAccResponse.class);
        Integer accountCreditId = createAnAccDebetResponse.getId();
        float balanceCredit = createAnAccDebetResponse.getBalance();

        //Предусловие шаг 4: пополненим баланс на счет-дебет.
        //сделаем депозит дважды. Чтобы потом проверить перевод на максимальную сумму - 10 000
        float balanceDebet = 0.0F;//создадим переменную для записи баланса счета дебета, чтобы потом считать баланс после перевода
        for (int i = 1; i <= 2; i++) {
            MakeDepositResponse makeDepositResponse = new MakeDepositRequester
                    (RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()), ResponseSpecs.isOk())
                    .post(new MakeDeposit
                            .Builder()
                            .setBalance(MaxSumsForDepositAndTransactions.DEPOSIT.getValue())
                            .setId(accountDebetId) //сделали депозит на счет-дебет, откуда будем переводить
                            .build()
                    ).extract().as(MakeDepositResponse.class);
            balanceDebet = makeDepositResponse.getBalance();
        }


        //Шаг 5: перевод
        TransferMoneyResponse transferMoneyResponse = new TransferMoneyRequester(RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.isOk())
                .post(new TransferMoney
                        .Builder()
                        .amount(MaxSumsForDepositAndTransactions.TRANSACTION.getValue())
                        .senderAccountId(accountDebetId)
                        .receiverAccountId(accountCreditId)
                        .build()).extract().as(TransferMoneyResponse.class);
        soflty.assertThat(transferMoneyResponse.getSenderAccountId()).isEqualTo(accountDebetId);
        soflty.assertThat(transferMoneyResponse.getReceiverAccountId()).isEqualTo(accountCreditId);
        soflty.assertThat(transferMoneyResponse.getAmount()).isEqualTo(MaxSumsForDepositAndTransactions.TRANSACTION.getValue());
        soflty.assertThat(transferMoneyResponse.getMessage()).isEqualTo(ServiceMessages.SUCCESSFUL_TRANSFER.getMessage());


        //проверка через запрос GetCustomerAccounts
        //получим все счета пользователя
        List<GetCustomerAccountResponse> accountsResponse = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.isOk())
                .get()
                .extract().as(new TypeRef<List<GetCustomerAccountResponse>>() {});
        List<GetCustomerAccountResponse> accounts = accountsResponse.stream().toList();

        //найдем среди всех счетов счёт-дебет и запишем его в переменную
        GetCustomerAccountResponse targetDebAcc = accounts.stream()
                .filter(a -> accountDebetId.equals(a.getId())) //номер счета
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //проверки первого уровня. Убедимся, что текущий баланс = начальный баланс - 5000
        soflty.assertThat(targetDebAcc.getBalance()).isEqualTo(balanceDebet - MaxSumsForDepositAndTransactions.TRANSACTION.getValue());

        //теперь смотрим транзакции этого счета. Ищем ту, где есть нужные совпадения по типу транзакции и сумме
        GetCustomerAccountResponse.Transaction targetDebetTransaction = targetDebAcc.getTransactions().stream()
                .filter(t -> t.getAmount() == MaxSumsForDepositAndTransactions.TRANSACTION.getValue()) //сумма перевода
                .filter(t -> t.getType().equals(TransactionType.TRANSFER_OUT.getMessage()))
                .filter(t -> t.getRelatedAccountId().equals(accountCreditId)) //вставить сюда счет кредита куда слали
                .findAny()
                .orElseThrow(() -> new AssertionError("Нужная транзакция не найдена"));

        //теперь найдем среди всех счетов счет-кредит и запишем его в переменную
        GetCustomerAccountResponse targetCredAcc = accounts.stream()
                .filter(a -> accountCreditId.equals(a.getId())) //номер счета
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //проверки первого уровня. Убедимся, что текущий баланс = начальный баланс + 5000
        soflty.assertThat(targetCredAcc.getBalance()).isEqualTo(balanceCredit + MaxSumsForDepositAndTransactions.TRANSACTION.getValue());

        //теперь смотрим транзакции этого счета. Ищем ту, где есть нужные совпадения по типу транзакции и сумме
        GetCustomerAccountResponse.Transaction targetCreditTransaction = targetCredAcc.getTransactions().stream()
                .filter(t -> t.getAmount() == MaxSumsForDepositAndTransactions.TRANSACTION.getValue()) //сумма перевода
                .filter(t -> t.getType().equals(TransactionType.TRANSFER_IN.getMessage()))
                .filter(t -> t.getRelatedAccountId().equals(accountDebetId)) //вставить сюда счет дебета откуда слали
                .findAny()
                .orElseThrow(() -> new AssertionError("Нужная транзакция не найдена"));


        //шаг 6: удаляем юзера
        //здесь создали только 1 юзера, его и удалим по id. Возьмем его из newUserResponse
        String successMessage = new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk())
                .delete(new DeleteByUserId(newUserResponse1.getId()))
                .extract().asString();

        String expected = String.format("User with ID %d deleted successfully.", newUserResponse1.getId());
        soflty.assertThat(successMessage).isEqualTo(expected);

        //Убедимся, что такого юзера нет. Админ вызывает getAllUsers, сразу положим в лист
        List<GetAllUsersResponse> users = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().as(new TypeRef<List<GetAllUsersResponse>>() {});

        //вытащим их id в лист
        List<Integer> userIds = users.stream()
                .map(GetAllUsersResponse::getId)
                .toList();

        //проверим, что такого id нет
        soflty.assertThat(userIds)
                .as("удалённый id не должен быть среди пользователей")
                .doesNotContain(newUserResponse1.getId());

    }

}
