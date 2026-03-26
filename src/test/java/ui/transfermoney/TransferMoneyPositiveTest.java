package ui.transfermoney;

import api.generators.MaxSumsForDepositAndTransactions;
import api.models.CreateAnAccountResponse;
import api.models.CreatedUser;
import api.models.MakeDepositResponse;
import api.requests.steps.UserSteps;
import common.annotation.Browsers;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.pages.TransferMoneyPage;
import ui.pages.UserDashboard;
import ui.alerts.AlertsHelpMethods;

public class TransferMoneyPositiveTest extends BaseUiTest {


    @Test
    @UserSession
    @Browsers({"firefox"})
    public void pageHasCorrectPageName() {

        new TransferMoneyPage()
                .open()
                .checkPageName();
    }

    @Test
    @UserSession
    @Browsers({"firefox"})
    public void userCanGoFromMakeTransferToDashboard() {

        new TransferMoneyPage()
                .open()
                .clickHomeButtonToGoToDashboard()
                .elementsAreVisible();
    }

    @Test
    @UserSession
    @Browsers({"firefox"})
    public void userCanGoToTransferAgainClickingButton() {

        new TransferMoneyPage()
                .open()
                .openTransferAgain();
        //.elementsAreVisible();
    }

    @Test
    @UserSession
    @Browsers({"firefox"})
    public void userCanGoFromDashboardToMakeTransfer() {

        new UserDashboard()
                .open()
                .clickTransferMoneyButton()
                .checkIfElementsAreVisible();
    }


    @Test
    @UserSession(value = 2)
    @Browsers({"firefox"})
    public void userSeesChangedBalanceAfterTransferInAcc() {
        /*

### Тест: после отправки платежа в Select Your Account баланс обновляется

Результат: ошибка! баланс не совпадает с действительным -- фронтенд не обновил страницу динамически, запрос /accounts или  /users

*/

        CreatedUser user1 = SessionStorage.getUser(1);
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), user1.getRequest());


        CreatedUser user2 = SessionStorage.getUser(2);
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        String user2Name = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();


        authAsUserUi(user1.getRequest());

        new TransferMoneyPage()
                .openTransferMoneyForm()
                .selectSenderAccount(accountResponse.getAccountNumber())
                .enterRecipientName(user2Name)
                .enterRecipientAccount(accountResponse2.getAccountNumber())
                .enterAmount(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .selectEmptyConfirmationCheckbox()
                .submit()
                .checkAlertMessageAndAccept(
                        AlertsHelpMethods.formTransferSuccessfulAlert(MaxSumsForDepositAndTransactions.DEPOSIT.getMax(), accountResponse2.getAccountNumber()))
                .checkBalanceAfterSuccessTransaction(makeDepositResponse.getBalance()); //подумать потом еще, ответственность какого класса этот метод

        //ошибка: баланс остался такой же на странице после отправки платежа

    }

    @Test
    @UserSession(value = 2)
    @Browsers({"firefox"})
    public void userCanTransferMoneyToUserWithRecipientName() {


        CreatedUser user1 = SessionStorage.getUser(1);

        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX2(accountResponse1.getId(), user1.getRequest());
        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());


        CreatedUser user2 = SessionStorage.getUser(2);
        String recipientName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());


        new TransferMoneyPage()
                .openTransferMoneyForm()
                .selectSenderAccount(accountResponse1.getAccountNumber())
                .enterRecipientName(recipientName)
                .enterRecipientAccount(accountResponse2.getAccountNumber())
                .enterAmount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .selectEmptyConfirmationCheckbox()
                .submit()
                .checkAlertMessageAndAccept(AlertsHelpMethods.formTransferSuccessfulAlert(MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                        accountResponse2.getAccountNumber()));

        //у юзера 1 убавился баланс
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        soflty.assertThat(user1BalanceAfter).isEqualTo(user1BalanceBefore - MaxSumsForDepositAndTransactions.TRANSACTION.getMax());

        //у юзера 2 прибавился баланс
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());
        soflty.assertThat(user2BalanceAfter).isEqualTo(user2BalanceBefore + MaxSumsForDepositAndTransactions.TRANSACTION.getMax());

    }

    @Test
    @UserSession(value = 2)
    @Browsers({"firefox"})
    public void userCanTransferMoneyToUserIfRecipientDoesntHaveName() {

        CreatedUser user1 = SessionStorage.getUser(1);
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX2(accountResponse1.getId(), user1.getRequest());
        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());


        CreatedUser user2 = SessionStorage.getUser(2);
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        new TransferMoneyPage()
                .openTransferMoneyForm()
                .selectSenderAccount(accountResponse1.getAccountNumber())
                .enterRecipientAccount(accountResponse2.getAccountNumber())
                .enterAmount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .selectEmptyConfirmationCheckbox()
                .submit()
                .checkAlertMessageAndAccept(AlertsHelpMethods.formTransferSuccessfulAlert(MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                        accountResponse2.getAccountNumber()))
        ;

        //у юзера 1 убавился баланс
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        soflty.assertThat(user1BalanceAfter).isEqualTo(user1BalanceBefore - MaxSumsForDepositAndTransactions.TRANSACTION.getMax());

        //у юзера 2 прибавился баланс
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());
        soflty.assertThat(user2BalanceAfter).isEqualTo(user2BalanceBefore + MaxSumsForDepositAndTransactions.TRANSACTION.getMax());
    }
}
