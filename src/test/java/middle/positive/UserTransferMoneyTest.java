package Tests.positive;

import Requests.*;
import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import Tests.BaseTest;
import generators.*;
import models.*;
import org.junit.jupiter.api.Test;

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

//создаем счет юзеру 1 и записываем его в переменную
        Integer accountDebetId = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract()
                .path("id");


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


        //Выведем 2 переменные: счет дебета и баланс счета дебета


        //перевод
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
        List<GetCustomerAccountResponse> accountsСredit = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.isOk())
                .get().extract()
                .jsonPath()
                .getList("", GetCustomerAccountResponse.class);

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

        //Удаляем юзеров
        //вернем айди всех и удалим всех
        List<Integer> ids = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().jsonPath().getList("id",  Integer.class);

        //удалим всех
        //удалим всех
        List<Integer> userIds = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().jsonPath().getList("id",  Integer.class);

        for (Integer id : userIds){
            new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk()).delete(new DeleteByUserId(id));

        }


    }

    //Тест 2: юзер переводит средства между своими счетами
    @Test
    public void userTransfersMoneyBetweenItsAccounts(){
        //создаем юзера
        NewUserRequest newUser1 = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser1);


        //создадим два счета. Этот счет будем дебетом и запишем его в переменную
        Integer accountDebetId = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract()
                .path("id");

        //создаем второй счет, который будет кредитом. Выпишем id счета и баланс для дальнейших сравнений
        CreateAnAccResponse createAnAccDebetResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract().as(CreateAnAccResponse.class);
        Integer accountCreditId = createAnAccDebetResponse.getId();
        float balanceCredit = createAnAccDebetResponse.getBalance();

        //пополненим баланс на счет-дебет. Через енум подтянули максимальную сумму для депозита.
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


        //перевод
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
        List<GetCustomerAccountResponse> accounts = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.isOk())
                .get().extract()
                .jsonPath()
                .getList("", GetCustomerAccountResponse.class);

        //найдем среди всех счетов счёт-дебет и запишем его в переменную
        GetCustomerAccountResponse targetDebAcc = accounts.stream()
                .filter(a -> accountDebetId.equals(a.getId())) //номер счета
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        // //проверки первого уровня. Убедимся, что текущий баланс = начальный баланс - 5000
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
        //Удаляем юзеров
        //вернем айди всех и удалим всех
        List<Integer> ids = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().jsonPath().getList("id",  Integer.class);

        //удалим всех
        List<Integer> userIds = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().jsonPath().getList("id",  Integer.class);

        for (Integer id : userIds){
            new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk()).delete(new DeleteByUserId(id));

        }

    }

}
