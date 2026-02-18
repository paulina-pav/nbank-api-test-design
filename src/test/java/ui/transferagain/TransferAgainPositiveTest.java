package ui.transferagain;

import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.TransactionType;
import api.models.CreateAnAccountResponse;
import api.models.MakeDepositResponse;
import api.models.TransferMoneyResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.models.CreatedUser;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.TransferAgainPage;
import ui.alerts.AlertsHelpMethods;

public class TransferAgainPositiveTest extends BaseUiTest {
    @Test
    @UserSession
    public void transferAgainCheckPage() {

        new TransferAgainPage()
                .open()
                .elementsAreVisible();

    }

    @Test
    @UserSession(value = 2)
    public void userCanSeeTransferInTransaction() {

        CreatedUser userCredit = SessionStorage.getUser(1);
        CreateAnAccountResponse accountResponseCredit = UserSteps.createsAccount(userCredit.getRequest());

        CreatedUser userDebet = SessionStorage.getUser(2);
        CreateAnAccountResponse accountResponseDebet = UserSteps.createsAccount(userDebet.getRequest());
        UserSteps.makesDepositX2(accountResponseDebet.getId(), userDebet.getRequest());

        TransferMoneyResponse transferMoneyResponse = UserSteps.transferMoney(accountResponseDebet.getId(), accountResponseCredit.getId(),
                MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), userDebet.getRequest());

        new TransferAgainPage()
                .open()
                .findAndClickTransaction(TransactionType.TRANSFER_IN.getMessage(), transferMoneyResponse.getAmount());

    }

    @Test
    @UserSession(value = 2)
    public void userCanSeeTransferOutTransaction() {

        CreatedUser user1 = SessionStorage.getUser(1);
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        UserSteps.makesDepositX2(accountResponse.getId(), user1.getRequest());

        CreatedUser user2 = SessionStorage.getUser(2);
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());

        TransferMoneyResponse transferMoneyResponse = UserSteps.transferMoney(accountResponse.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest());

        new TransferAgainPage()
                .open()
                .findAndClickTransaction(TransactionType.TRANSFER_OUT.getMessage(), transferMoneyResponse.getAmount());

    }

    @Test
    @UserSession
    public void userCanSeeDepositTransaction() {


        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(SessionStorage.getUser().getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), SessionStorage.getUser().getRequest());


        new TransferAgainPage()
                .open()
                .findAndClickTransaction(TransactionType.DEPOSIT.getMessage(), makeDepositResponse.getBalance());

    }

    @Test
    @UserSession(value = 2)
    public void userCanSearchTransactionByRecipientName() {
        /*
### Тест: Юзер-отправитель может найти транзакцию по имени получателя в в разделе Transfer Again

Ошибка! при поиске находится транзакция трансфер ин -- я вижу транзакции своего получателя(или подменился тип)! UI не так парсит респонз /users
Потенциальная проблема: запрос не динамический, если ты в другом окне сделал перевод, то в этом он не появится, потому что страница не обновляется в процессе
         */

        CreatedUser user1 = SessionStorage.getUser(1);
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        UserSteps.makesDepositX2(accountResponse.getId(), user1.getRequest());


        CreatedUser user2 = SessionStorage.getUser(2);
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        String user2Name = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();

        UserSteps.transferMoney(accountResponse.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest());

        new TransferAgainPage()
                .open()
                .findTransactionByName(user2Name);

        //Проблемы: 1)транзакция должна быть трансфер аут, тк мы смотрим у юзера-отправителя
        // 2) не было имени ДО момента поиска, а когда транзакция нашлась, имя появилось

    }

    @Test
    @UserSession(value = 2)
    public void userCanSearchTransactionBySenderUsername() {

    /*
### Тест: юзер-получатель может найти транзакцию трансфер-ин по юзернейму отправителя

Ошибка! при поиске находится транзакция трансфер аут -- я вижу транзакции от лица своего отправителя!
     */

        CreatedUser user1 = SessionStorage.getUser(2);
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        UserSteps.makesDepositX2(accountResponse.getId(), user1.getRequest());
        String user1Name = UserSteps.changesNameReturnRequest(user1.getRequest()).getName();


        CreatedUser user2 = SessionStorage.getUser(1);
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        UserSteps.transferMoney(accountResponse.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest());

        new TransferAgainPage()
                .open()
                .findTransactionByName(user1Name);

        //Ошибка:транзакция должна быть трансфер ин, тк мы смотрим у юзера-получателя

    }

    @Test
    @UserSession
    public void userCanGoFromTransferAgainToNewTransfer() {

        new TransferAgainPage()
                .open()
                .clickNewTransferButton()
                .elementsAreVisible();
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
    @UserSession
    public void transferAgainModalCheck() {

        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(SessionStorage.getUser().getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), SessionStorage.getUser().getRequest());

        new TransferAgainPage()
                .open()
                .findAndClickTransaction(TransactionType.DEPOSIT.getMessage(), makeDepositResponse.getBalance());
    }

    @Test
    @UserSession(value = 2)
    public void userCanTransferOutAgain() {
        /*

### Тест: юзер выбирает трансфер аут и отправляет еще раз

Результат:
Ошибка! Запрос уходит самому себе ✅ Transfer of $9 successful from Account 1 to 1!
Появились 2 транзакции: трансфер ин и трансфер аут, когда прислал сам себе!

         */

        CreatedUser user1 = SessionStorage.getUser(1);
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX4(accountResponse1.getId(), user1.getRequest());

        CreatedUser user2 = SessionStorage.getUser(2);
        String recipientName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());

        TransferMoneyResponse transferMoneyResponse = UserSteps.transferMoney(accountResponse1.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest());

        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());


        new TransferAgainPage()
                .open()
                .findAndClickTransaction(TransactionType.TRANSFER_OUT.getMessage(), transferMoneyResponse.getAmount())
                .findConfirmtionTextTransferToAccount(accountResponse2.getId())
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

    @Test
    @UserSession
    public void userCanMakeDepositInTransferAgain() {
        /*
### Тест: Повторить депозит через Transfer Again
Предшаги через апи: админ создает юзера 1. Сделать ему депозит 5000.

Шаг: юзер 1 логинится и жмет кнопку Make a Transfer, а затем Transfer Again. Имеется транзакция депозит. Нажать Repeat Transfer. Выбрать счет и сумму.
Результат: ошибка, под капотом запрос /transfer, а не /deposit. + появляются 2 транзакции in/out в списке, баланс не увеличился
         */

        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(SessionStorage.getUser().getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDeposit(accountResponse1.getId(), SessionStorage.getUser().getRequest());

        Double user1BalanceBefore = UserSteps.getBalance(SessionStorage.getUser().getRequest(), accountResponse1.getId());


        new TransferAgainPage()
                .open()
                .findAndClickTransaction(TransactionType.DEPOSIT.getMessage(), depositResponse1.getBalance())
                .selectYourAccount(accountResponse1.getAccountNumber())
                .findConfirmtionTextTransferToAccount(accountResponse1.getId())
                .insertAmount(depositResponse1.getBalance())
                .confirm()
                .sendTransfer()
                .checkAlertMessageAndAccept(AlertsHelpMethods.formTransferAgainDebetSuccessfulAlert(depositResponse1.getBalance(),
                        accountResponse1.getAccountNumber())
                );

        Double user1BalanceAfter = UserSteps.getBalance(SessionStorage.getUser().getRequest(), accountResponse1.getId());

        //ошибка: появляются 2 транзакции трансфер ин и трансфер аут, баланс в итоге не меняется
        soflty.assertThat(user1BalanceBefore).isEqualTo(user1BalanceAfter);

    }

}
