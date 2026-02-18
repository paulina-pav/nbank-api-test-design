package ui.makedeposit;

import api.models.CreateAnAccountResponse;
import api.requests.steps.UserSteps;
import api.models.CreatedUser;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.MakeDeposit;
import ui.alerts.DepositAlerts;

public class MakeDepositNegativeTest extends BaseUiTest {
    @Test
    public void userCantMakeDepositIfDoesntChooseAcc() {
        /*
### Тест: При невыбранном счете появляется ошибка

Результат: ❌ Please select an account. (Запроса к апи нет, проверка на фронте)
         */
        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());

        new MakeDeposit()
                .open()
                .clickTheDepositButton()
                .checkAlertMessageAndAccept(DepositAlerts.SELECT_ACCOUNT.getMessage());
    }

    @Test
    public void userCantMakeDepositWithInvalidSum() {
        /*
        ### Тест: При сумме 5001 ошибка

Результат: ❌ Please deposit less or equal to 5000$.(Запроса к апи нет, проверка на фронте)
         */

        CreatedUser user = createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user.getRequest());

        authAsUserUi(user.getRequest());

        new MakeDeposit()
                .open()
                .selectAccount(accountResponse.getAccountNumber())
                .enterAmount(5001.0)
                .clickTheDepositButton()
                .checkAlertMessageAndAccept(DepositAlerts.DEPOSIT_LESS_THAN_5001.getMessage());
    }

    @Test
    public void userCantMakeDepositWitZero() {

        /*
### Тест: При сумме 0 появляется ошибка
Результат: ❌ Please enter a valid amount. (Запроса к апи нет, проверка на фронте)
         */

        CreatedUser user = createUser();

        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user.getRequest());

        authAsUserUi(user.getRequest());

        new MakeDeposit()
                .open()
                .selectAccount(accountResponse.getAccountNumber())
                .enterAmount(0.0)
                .clickTheDepositButton()
                .checkAlertMessageAndAccept(DepositAlerts.ENTER_VALID_AMOUNT.getMessage());
    }
}
