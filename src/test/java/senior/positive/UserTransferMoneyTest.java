package senior.positive;


import Tests.BaseTest;
import api.generators.MaxSumsForDepositAndTransactions;

import api.generators.ServiceMessages;
import api.generators.TransactionType;
import api.models.*;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.ArrayList;
import java.util.List;


public class UserTransferMoneyTest extends BaseTest {

//Тест-кейсы этого файла:
//1. Юзер успешно переводит деньги на существующий счет другого юзера
//2. Юзер успешно переводит деньги с одного своего счета на другой

    @Test
    public void userTransferMoneyFromOneAccToAnotherAcc() {
        NewUserRequest newUser = AdminSteps.createUser();

        //Предусловие шаг 2: юзер создает 2 акк.
        List<CreateAnAccountResponse> newAccounts = new ArrayList<>();
        for (int i = 0; i <= 1; i++) {
            CreateAnAccountResponse account = UserSteps.createsAccount(newUser);
            newAccounts.add(account);
        }

        //Пополним баланс предполагаемому счету-дебету
        for (int i = 0; i <= 1; i++) {
            MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(newAccounts.get(0).getId(), newUser);
        }

        //выпишем счета
        Long debetId = newAccounts.get(0).getId();
        Long creditId = newAccounts.get(1).getId();

        //Запросим информацию о счетах после пополнения, чтобы зафиксировать их балансы до транзакции
        List<GetCustomerAccountResponse> allAccsBefore = UserSteps.getsAccounts(newUser);
        //найдем счет-дебет
        GetCustomerAccountResponse debetBefore = allAccsBefore.stream()
                .filter(a -> a.getId().equals(debetId))
                .findAny()
                .orElseThrow(() -> new AssertionError("счета не существует"));
        //зафиксируем его баланс до перевода
        Double balanceDebetBeforeTransfer = debetBefore.getBalance();

        //найдем счет-кредит
        GetCustomerAccountResponse creditBefore = allAccsBefore.stream()
                .filter(a -> a.getId().equals(creditId))
                .findAny()
                .orElseThrow(() -> new AssertionError("счета не существует"));
        //зафиксируем его баланс до перевода
        Double balanceCreditBeforeTransfer = creditBefore.getBalance();


        //Шаг 5: перевод на др свой счет
        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(newAccounts.get(0).getId())
                .amount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .receiverAccountId(newAccounts.get(1).getId())
                .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK()
        ).post(transferMoney);
        soflty.assertThat(transferMoneyResponse.getSenderAccountId()).isEqualTo(newAccounts.get(0).getId());
        soflty.assertThat(transferMoneyResponse.getReceiverAccountId()).isEqualTo(newAccounts.get(1).getId());
        soflty.assertThat(transferMoneyResponse.getAmount()).isEqualTo(MaxSumsForDepositAndTransactions.TRANSACTION.getMax());
        soflty.assertThat(transferMoneyResponse.getMessage()).isEqualTo(ServiceMessages.SUCCESSFUL_TRANSFER.getMessage());

        //I. Баланс у счета дебета уменьшился на искомую сумму, а у кредита -- увеличится на столько же и текущие балансы в целом верны

        List<GetCustomerAccountResponse> allAccsAfter = UserSteps.getsAccounts(newUser);

        GetCustomerAccountResponse debetAfter = allAccsAfter.stream()
                .filter(a -> a.getId().equals(debetId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Дебетовый счет после перевода не найден"));

        GetCustomerAccountResponse creditAfter = allAccsAfter.stream()
                .filter(a -> a.getId().equals(creditId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Кредитовый счет после перевода не найден"));

        double expectedDebetAfter = balanceDebetBeforeTransfer - MaxSumsForDepositAndTransactions.TRANSACTION.getMax();
        double expectedCreditAfter = balanceCreditBeforeTransfer + MaxSumsForDepositAndTransactions.TRANSACTION.getMax();

        soflty.assertThat(debetAfter.getBalance())
                .as("Баланс дебета после перевода")
                .isEqualTo(expectedDebetAfter);

        soflty.assertThat(creditAfter.getBalance())
                .as("Баланс кредита после перевода")
                .isEqualTo(expectedCreditAfter);


        //II. Проверка, что у каждого из счетов была соответствующая транзакция на соответствующую сумму
        List<GetAccountTransactionsResponse> debetTransactions = UserSteps.getsAccountTransaction(newUser, debetId);

        //Счет дебет: есть транзакция трансфер аут и на 10000
        debetTransactions.stream()
                .filter(t -> TransactionType.TRANSFER_OUT.getMessage().equals(t.getType()))
                .filter(t -> t.getRelatedAccountId().equals(creditId))
                .filter(t -> t.getAmount().equals(t.getAmount() - expectedDebetAfter))
                .findAny()
                .orElseThrow(() -> new AssertionError("TRANSFER_OUT транзакция не найдена"));

        //счет-кредит: есть транзакция трансфер ин и на 10000
        List<GetAccountTransactionsResponse> creditTransactions = UserSteps.getsAccountTransaction(newUser, creditId);
        creditTransactions.stream()
                .filter(t -> TransactionType.TRANSFER_IN.getMessage().equals(t.getType()))
                .filter(t -> t.getRelatedAccountId().equals(debetId))
                .filter(t -> t.getAmount().equals(t.getAmount() + expectedDebetAfter))
                .findAny()
                .orElseThrow(() -> new AssertionError("TRANSFER_IN транзакция не найдена"));

        //Пост условие: удалить созданного юзера
        AdminSteps.deletesUser(newUser);

    }


    @Test
    // Юзер успешно переводит деньги на существующий счет другого юзера
    public void userTransfersMoneyToUser() {
        //cоздать 2 юзеров
        List<NewUserRequest> users = new ArrayList<>();
        for (int i = 0; i <= 1; i++) {
            NewUserRequest newUser = AdminSteps.createUser();
            users.add(newUser);
        }
        NewUserRequest userDeb = users.get(0);
        NewUserRequest userCred = users.get(1);


        //создать счет юзеру-кредиту
        CreateAnAccountResponse userCreditAccount = UserSteps.createsAccount(userCred);

        //создать счет юзеру-дебету
        CreateAnAccountResponse userDebetAccount = UserSteps.createsAccount(userDeb);

        //пополнить счет юзеру-дебету
        for (int i = 0; i <= 1; i++) {
            MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(userDebetAccount.getId(), userDeb);
        }

        //выпишем счета
        Long debetId = userDebetAccount.getId();
        Long creditId = userCreditAccount.getId();


        //Запросим информацию о счетах после пополнения, чтобы зафиксировать их балансы до транзакции
        //Дебет:
        List<GetCustomerAccountResponse> allAccsUserDebetBefore = UserSteps.getsAccounts(userDeb);
        //найдем счет-дебет
        GetCustomerAccountResponse debetBefore = allAccsUserDebetBefore.stream()
                .filter(a -> a.getId().equals(debetId))
                .findAny()
                .orElseThrow(() -> new AssertionError("счета не существует"));
        //зафиксируем его баланс до перевода
        Double balanceDebetBeforeTransfer = debetBefore.getBalance();


        //найдем счет-кредит
        List<GetCustomerAccountResponse> allAccsUserCreditBefore = UserSteps.getsAccounts(userCred);

        GetCustomerAccountResponse creditBefore = allAccsUserCreditBefore.stream()
                .filter(a -> a.getId().equals(creditId))
                .findAny()
                .orElseThrow(() -> new AssertionError("счета не существует"));
        //зафиксируем его баланс до перевода
        Double balanceCreditBeforeTransfer = creditBefore.getBalance();


        //Перевод от юзера-дебета к юзеру-кредиту
        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .amount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .receiverAccountId(creditId)
                .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(userDeb.getUsername(), userDeb.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK()
        ).post(transferMoney);
        soflty.assertThat(transferMoneyResponse.getSenderAccountId()).isEqualTo(debetId);
        soflty.assertThat(transferMoneyResponse.getReceiverAccountId()).isEqualTo(creditId);
        soflty.assertThat(transferMoneyResponse.getAmount()).isEqualTo(MaxSumsForDepositAndTransactions.TRANSACTION.getMax());
        soflty.assertThat(transferMoneyResponse.getMessage()).isEqualTo(ServiceMessages.SUCCESSFUL_TRANSFER.getMessage());

        //I. Баланс у счета дебета уменьшился на искомую сумму, а у кредита -- увеличится на столько же; и текущие балансы в целом верны
        //юзер-дебет
        List<GetCustomerAccountResponse> DebetAccsAfter = UserSteps.getsAccounts(userDeb);

        GetCustomerAccountResponse debetAfter = DebetAccsAfter.stream()
                .filter(a -> a.getId().equals(debetId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Дебетовый счет после перевода не найден"));

        double expectedDebetAfter = balanceDebetBeforeTransfer - MaxSumsForDepositAndTransactions.TRANSACTION.getMax();

        //юзер-кредит
        List<GetCustomerAccountResponse> CreditAccsAfter = UserSteps.getsAccounts(userCred);
        GetCustomerAccountResponse creditAfter = CreditAccsAfter.stream()
                .filter(a -> a.getId().equals(creditId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Кредитовый счет после перевода не найден"));

        double expectedCreditAfter = balanceCreditBeforeTransfer + MaxSumsForDepositAndTransactions.TRANSACTION.getMax();

        soflty.assertThat(debetAfter.getBalance())
                .as("Баланс дебета после перевода")
                .isEqualTo(expectedDebetAfter);

        soflty.assertThat(creditAfter.getBalance())
                .as("Баланс кредита после перевода")
                .isEqualTo(expectedCreditAfter);

        //II. Проверка, что у каждого из счетов была соответствующая транзакция на соответствующую сумму
        List<GetAccountTransactionsResponse> debetTransactions = UserSteps.getsAccountTransaction(userDeb, debetId);

        //Счет дебет: есть транзакция трансфер аут и на 10000
        debetTransactions.stream()
                .filter(t -> TransactionType.TRANSFER_OUT.getMessage().equals(t.getType()))
                .filter(t -> t.getRelatedAccountId().equals(creditId))
                .filter(t -> t.getAmount().equals(t.getAmount() - expectedDebetAfter))
                .findAny()
                .orElseThrow(() -> new AssertionError("TRANSFER_OUT транзакция не найдена"));

        //счет-кредит: есть транзакция трансфер ин и на 10000
        List<GetAccountTransactionsResponse> creditTransactions = UserSteps.getsAccountTransaction(userCred, creditId);

        creditTransactions.stream()
                .filter(t -> TransactionType.TRANSFER_IN.getMessage().equals(t.getType()))
                .filter(t -> t.getRelatedAccountId().equals(debetId))
                .filter(t -> t.getAmount().equals(t.getAmount() + expectedDebetAfter))
                .findAny()
                .orElseThrow(() -> new AssertionError("TRANSFER_IN транзакция не найдена"));

        //удалить пользователей
        for (NewUserRequest u : users) {
            AdminSteps.deletesUser(u);
        }
    }
}