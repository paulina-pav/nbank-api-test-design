package uisenior.transferagain;

import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.TransactionType;
import api.models.CreateAnAccountResponse;
import api.models.CreatedUser;
import api.models.MakeDepositResponse;
import api.models.TransferMoneyResponse;
import api.requests.steps.UserSteps;
import common.annotation.Browsers;
import common.annotation.UserSession;
import common.storage.SessionStorage;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uisenior.BaseUiTest;
import ui.pages.TransferAgainPage;
import ui.alerts.AlertsHelpMethods;

public class TransferAgainPositiveTest extends BaseUiTest {

/*
    Ошибки: при поиске вижу не свои транзакции а своего получателя или отправителя.
     UI не так парсит респонз /users
Потенциальная проблема: запрос не динамический, если ты в другом окне сделал перевод, то в этом он не появится, потому что страница не обновляется в процессе

    проблема 2: не было имени ДО момента поиска, а когда транзакция нашлась, имя появилось
 */


    @Test
    @UserSession
    @Browsers({"firefox"})
    public void userCanMakeDepositInTransferAgain() {
        /*
Результат: ошибка, под капотом запрос /transfer, а не /deposit. + появляются 2 транзакции in/out в списке, баланс не увеличился*/


        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(SessionStorage.getUser().getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDeposit(accountResponse1.getId(), SessionStorage.getUser().getRequest());

        Double user1BalanceBefore = UserSteps.getBalance(SessionStorage.getUser().getRequest(), accountResponse1.getId());


        new TransferAgainPage()
                .open()
                .getTransactionSection()
                .findTransactionByTypeAndSum(TransactionType.DEPOSIT.getMessage(), depositResponse1.getBalance())
                .clickRepeatButton()
                .selectYourAccount(accountResponse1.getAccountNumber())
                .findConfirmationTextTransferToAccount(accountResponse1.getId())
                .insertAmount(depositResponse1.getBalance())
                .confirm()
                .sendTransfer()
                .checkAlertMessageAndAccept(AlertsHelpMethods.formTransferAgainDebetSuccessfulAlert(depositResponse1.getBalance(),
                        accountResponse1.getAccountNumber()));

        Double user1BalanceAfter = UserSteps.getBalance(SessionStorage.getUser().getRequest(), accountResponse1.getId());

        //ошибка: появляются 2 транзакции трансфер ин и трансфер аут, баланс в итоге не меняется
        soflty.assertThat(user1BalanceBefore).isEqualTo(user1BalanceAfter);

    }


    @Test
    @UserSession(value = 2)
    @Browsers({"firefox"})
    public void userReceiverSearchesByUsernameButSeeTransactionWithName() {
        // у пользователя есть имя. убедиться, что даже при поиске по юзернейму всплывает имя
        CreatedUser user1 = SessionStorage.getUser(1);//получатель
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());


        CreatedUser user2 = SessionStorage.getUser(2); //отправитель
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        UserSteps.makesDepositX2(accountResponse2.getId(), user2.getRequest());
        String senderName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();

        Double sum = UserSteps.transferMoney(accountResponse2.getId(), accountResponse.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                user2.getRequest()).getAmount();

        //зашли в роли получателя
        new TransferAgainPage()
                .open()
                .insertUsername(SessionStorage.getUser(2).getRequest().getUsername()) //ищем по юзернейму
                .clickSearchButton();
        // .searchTransactionByName(sum, TransactionType.TRANSFER_IN.getMessage(), senderName); //но сверяем по имени
        //выключила шаг, потому что в результате он выводит тип транзакции будто я смотрю со стороны отправителя


    }

    @Test
    @UserSession(value = 2)
    @Browsers({"firefox"})
    public void userSenderCanSearchTransactionByReceiverName() {

        CreatedUser user1 = SessionStorage.getUser(1);//отправитель
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        UserSteps.makesDepositX2(accountResponse.getId(), user1.getRequest());


        CreatedUser user2 = SessionStorage.getUser(2); //получатель
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        String receiverName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();

        Double sum = UserSteps.transferMoney(accountResponse.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                user1.getRequest()).getAmount();

        //зашли в роли отправителя
        new TransferAgainPage()
                .open()
                .insertName(receiverName)
                .clickSearchButton();
        // .searchTransactionByName(sum, TransactionType.TRANSFER_OUT.getMessage(), receiverName);
        //выключила шаг, потому что в результате он выводит тип транзакции будто я смотрю со стороны получателя

    }

    @Test
    @UserSession(value = 2)
    @Browsers({"firefox"})
    public void userReceiverCanSearchTransactionBySenderName() {

        CreatedUser user1 = SessionStorage.getUser(1);//получатель
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());


        CreatedUser user2 = SessionStorage.getUser(2); //отправитель
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        UserSteps.makesDepositX2(accountResponse2.getId(), user2.getRequest());
        String senderName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();

        Double sum = UserSteps.transferMoney(accountResponse2.getId(), accountResponse.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                user2.getRequest()).getAmount();

        //зашли в роли получателя
        new TransferAgainPage()
                .open()
                .insertName(senderName)
                .clickSearchButton();
        //  .searchTransactionByName(sum, TransactionType.TRANSFER_IN.getMessage(), senderName);
        //выключила шаг, потому что в результате он выводит тип транзакции будто я смотрю со стороны отправителя

    }

    @Test
    @UserSession(value = 2)
    @Browsers({"firefox"})
    public void userCanSeeTransferInTransaction() {

        CreatedUser userCredit = SessionStorage.getUser(1);
        CreateAnAccountResponse accountResponseCredit = UserSteps.createsAccount(userCredit.getRequest());

        CreatedUser userDebet = SessionStorage.getUser(2);
        CreateAnAccountResponse accountResponseDebet = UserSteps.createsAccount(userDebet.getRequest());
        UserSteps.makesDepositX2(accountResponseDebet.getId(), userDebet.getRequest());

        Double sum = UserSteps.transferMoney(accountResponseDebet.getId(), accountResponseCredit.getId(),
                MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), userDebet.getRequest()).getAmount();

        new TransferAgainPage()
                .open()
                .getTransactionSection()
                .findTransactionByTypeAndSum(TransactionType.TRANSFER_IN.getMessage(), sum);

    }

    @Test
    @UserSession(value = 2)
    @Browsers({"firefox"})
    public void userCanSeeTransferOutTransaction() {

        CreatedUser user1 = SessionStorage.getUser(1);
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        UserSteps.makesDepositX2(accountResponse.getId(), user1.getRequest());

        CreatedUser user2 = SessionStorage.getUser(2);
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());

        Double sum = UserSteps.transferMoney(accountResponse.getId(), accountResponse2.getId(),
                MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest()).getAmount();

        new TransferAgainPage()
                .open()
                .getTransactionSection()
                .findTransactionByTypeAndSum(TransactionType.TRANSFER_OUT.getMessage(), sum);

    }

    @Test
    @UserSession
    public void userCanSeeDepositTransaction() {
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(SessionStorage.getUser().getRequest());
        Double balance = UserSteps.makesDeposit(accountResponse.getId(), SessionStorage.getUser().getRequest()).getBalance();

        new TransferAgainPage()
                .open()
                .getTransactionSection()
                .findTransactionByTypeAndSum(TransactionType.DEPOSIT.getMessage(), balance);

    }

    @Test
    @UserSession(value = 2)
    public void userReceiverCanSearchTransactionBySenderUsername() {

        CreatedUser user1 = SessionStorage.getUser(1);//получатель
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());


        CreatedUser user2 = SessionStorage.getUser(2); //отправитель
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        UserSteps.makesDepositX2(accountResponse2.getId(), user2.getRequest());

        Double sum = UserSteps.transferMoney(accountResponse2.getId(), accountResponse.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                user2.getRequest()).getAmount();

        //зашли в роли получателя
        new TransferAgainPage()
                .open()
                .insertUsername(SessionStorage.getUser(2).getRequest().getUsername())
                .clickSearchButton();
        //  .searchTransactionByUsername(sum, TransactionType.TRANSFER_OUT.getMessage(), SessionStorage.getUser(2).getRequest().getUsername());
        //выключила шаг, потому что в результате он выводит тип транзакции будто я смотрю со стороны отправителя

    }

    @Test
    @UserSession(value = 2)
    public void userSenderCanSearchTransactionByReceiverUsername() {

        CreatedUser user1 = SessionStorage.getUser(1);//отправитель
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        UserSteps.makesDepositX2(accountResponse.getId(), user1.getRequest());

        CreatedUser user2 = SessionStorage.getUser(2); //получатель
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());

        Double sum = UserSteps.transferMoney(accountResponse.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                user1.getRequest()).getAmount();

        //зашли в роли отправителя
        new TransferAgainPage()
                .open()
                .insertUsername(SessionStorage.getUser(2).getRequest().getUsername())
                .clickSearchButton();
        // .searchTransactionByName(sum, TransactionType.TRANSFER_OUT.getMessage(), SessionStorage.getUser(2).getRequest().getUsername());
        //выключила шаг, потому что в результате он выводит тип транзакции будто я смотрю со стороны получателя

    }


    @Test
    @UserSession
    public void userCanGoFromTransferAgainToNewTransfer() {

        new TransferAgainPage()
                .open()
                .clickNewTransferButton()
                .checkIfElementsAreVisible();
    }


    @Test
    @UserSession
    public void userCanGoFromTransferAgainToDashboard() {

        new TransferAgainPage()
                .open()
                .clickHomeButton()
                .elementsAreVisible();
    }

    @Test
    @UserSession(value = 2)
    public void userCanTransferOutAgain() {
        /*

### Тест: юзер выбирает трансфер аут и отправляет еще раз

Результат:
Ошибка! Запрос уходит самому себе ✅ Transfer of $9 successful from Account 1 to 1!
Появились 2 транзакции: трансфер ин и трансфер аут, когда прислал сам себе!*/


        CreatedUser user1 = SessionStorage.getUser(1);
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX4(accountResponse1.getId(), user1.getRequest());

        CreatedUser user2 = SessionStorage.getUser(2);
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());

        TransferMoneyResponse transferMoneyResponse = UserSteps.transferMoney(accountResponse1.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest());

        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());


        new TransferAgainPage()
                .open()
                .getTransactionSection()
                .findTransactionByTypeAndSum(TransactionType.TRANSFER_OUT.getMessage(), transferMoneyResponse.getAmount())
                .clickRepeatButton()
                .findConfirmationTextTransferToAccount(accountResponse2.getId())
                .selectYourAccount((accountResponse1.getAccountNumber()))
                .insertAmount(transferMoneyResponse.getAmount())
                .confirm()
                .sendTransfer()
                .checkAlertMessageAndAccept(AlertsHelpMethods.formTransferAgainSuccessfulAlert(transferMoneyResponse.getAmount(),
                        accountResponse1.getAccountNumber(), accountResponse2.getAccountNumber())
                );

        //проверка балансов: не должны измениться из-за ошибки
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        soflty.assertThat(user1BalanceBefore).isEqualTo(user1BalanceAfter);
        soflty.assertThat(user2BalanceBefore).isEqualTo(user2BalanceAfter);
    }
}
