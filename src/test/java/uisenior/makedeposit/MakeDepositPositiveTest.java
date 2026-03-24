package uisenior.makedeposit;

import api.generators.MaxSumsForDepositAndTransactions;
import api.models.CreateAnAccountResponse;
import api.requests.steps.UserSteps;
import common.annotation.Browsers;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import uisenior.BaseUiTest;
import ui.pages.MakeDeposit;
import ui.pages.UserDashboard;
import ui.alerts.AlertsHelpMethods;

public class MakeDepositPositiveTest extends BaseUiTest {


    @Test
    @UserSession
    @Browsers({"firefox"})
    public void userCanGoFromDashboardToDepositMoneyPage() {

        new UserDashboard()
                .open()
                .clickMakeDepositButton()
                .checkIfTitleIsCorrect();
    }

    @Test
    @Browsers({"firefox"})
    @UserSession
    public void userCanClickHomeButtonGoToDashboard() {
        new MakeDeposit()
                .open()
                .clickHomeButton();
    }

    @Test
    @UserSession
    @Browsers({"firefox"})
    public void userCanMakeDeposit() {

        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(SessionStorage.getUser().getRequest());

        //зафиксируем баланс ДО
        Double balanceBefore = UserSteps.getBalance(SessionStorage.getUser().getRequest(), accountResponse.getId());

        new MakeDeposit()
                .openDepositSection()
                .selectAccount(accountResponse.getAccountNumber())
                .enterAmount(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .clickTheDepositButton()
                .checkAlertMessageAndAcceptAndGoToUserDashboard(AlertsHelpMethods.formDepositSuccessfulAlert(MaxSumsForDepositAndTransactions.DEPOSIT.getMax(),
                        accountResponse.getAccountNumber()))

        ;

        //баланс на уровне апи ПОСЛЕ
        Double balanceAfter = UserSteps.getBalance(SessionStorage.getUser().getRequest(), accountResponse.getId());

        //сверим, что баланс на уровне апи = сумме на которую делали депозит на UI
        soflty.assertThat(balanceAfter).isEqualTo(balanceBefore + MaxSumsForDepositAndTransactions.DEPOSIT.getMax());
    }
}
