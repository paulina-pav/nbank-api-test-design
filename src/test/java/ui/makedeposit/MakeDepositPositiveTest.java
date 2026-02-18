package ui.makedeposit;

import api.generators.MaxSumsForDepositAndTransactions;
import api.models.CreateAnAccountResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.models.CreatedUser;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.MakeDeposit;
import ui.UserDashboard;
import ui.alerts.AlertsHelpMethods;

public class MakeDepositPositiveTest extends BaseUiTest {
    @Test
    @UserSession
    public void depositMoneyPageCheck() {

        new MakeDeposit()
                .open()
                .elementsAreVisible();
    }

    @Test
    @UserSession
    public void userCanGoFromDashboardToDepositMoneyPage() {

        new UserDashboard()
                .open()
                .clickMakeDepositButton()
                .elementsAreVisible();
    }

    @Test
    @UserSession
    public void userCanMakeDeposit() {

        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(SessionStorage.getUser().getRequest());

        //зафиксируем баланс ДО
        Double balanceBefore = UserSteps.getBalance(SessionStorage.getUser().getRequest(), accountResponse.getId());

        new MakeDeposit()
                .open()
                .selectAccount(accountResponse.getAccountNumber())
                .enterAmount(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .clickTheDepositButton()
                .checkAlertMessageAndAccept(AlertsHelpMethods.formDepositSuccessfulAlert(MaxSumsForDepositAndTransactions.DEPOSIT.getMax(),
                        accountResponse.getAccountNumber()));

        //баланс на уровне апи ПОСЛЕ
        Double balanceAfter = UserSteps.getBalance(SessionStorage.getUser().getRequest(), accountResponse.getId());

        //сверим, что баланс на уровне апи = сумме на которую делали депозит на UI
        soflty.assertThat(balanceAfter).isEqualTo(MaxSumsForDepositAndTransactions.DEPOSIT.getMax());
    }
}
