package ui.makedeposit;

import api.generators.MaxSumsForDepositAndTransactions;
import api.models.CreateAnAccountResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.MakeDeposit;
import ui.UserDashboard;
import ui.alerts.AlertsHelpMethods;

public class MakeDepositPositiveTest extends BaseUiTest {
    @Test
    public void depositMoneyPageCheck() {

        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());

        new MakeDeposit()
                .open()
                .elementsAreVisible();
    }

    @Test
    public void userCanGoFromDashboardToDepositMoneyPage() {

        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());

        new UserDashboard()
                .open()
                .clickMakeDepositButton()
                .elementsAreVisible();
    }

    @Test
    public void userCanMakeDeposit() {
        CreatedUser user = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user.getRequest());

        //зафиксируем баланс ДО
        Double balanceBefore = UserSteps.getBalance(user.getRequest(), accountResponse.getId());

        authAsUserUi(user.getRequest());

        new MakeDeposit()
                .open()
                .selectAccount(accountResponse.getAccountNumber())
                .enterAmount(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .clickTheDepositButton()
                .checkAlertMessageAndAccept(AlertsHelpMethods.formDepositSuccessfulAlert(MaxSumsForDepositAndTransactions.DEPOSIT.getMax(),
                        accountResponse.getAccountNumber()));

        //баланс на уровне апи ПОСЛЕ
        Double balanceAfter = UserSteps.getBalance(user.getRequest(), accountResponse.getId());

        //сверим, что баланс на уровне апи = сумме на которую делали депозит на UI
        soflty.assertThat(balanceAfter).isEqualTo(MaxSumsForDepositAndTransactions.DEPOSIT.getMax());
    }
}
