package uisenior.makedeposit;

import api.models.CreateAnAccountResponse;
import api.requests.steps.UserSteps;
import common.annotation.Browsers;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uisenior.BaseUiTest;
import ui.pages.MakeDeposit;
import ui.alerts.DepositAlerts;

import java.util.stream.Stream;

public class MakeDepositNegativeTest extends BaseUiTest {
    @Test
    @UserSession
    @Browsers({"firefox"})
    public void userCantMakeDepositIfDoesntChooseAcc() {
        /*
### Тест: При невыбранном счете появляется ошибка
Результат: ❌ Please select an account. (Запроса к апи нет, проверка на фронте)
         */

        new MakeDeposit()
                .openDepositSection()
                .clickTheDepositButton()
                .checkAlertMessageAndAccept(DepositAlerts.SELECT_ACCOUNT.getMessage());
    }

    public static Stream<Arguments> invalidSums(){
        return Stream.of(
                Arguments.of(5001.0, DepositAlerts.DEPOSIT_LESS_THAN_5001.getMessage()),
                Arguments.of(0.0, DepositAlerts.ENTER_VALID_AMOUNT.getMessage())
        );
    }


    @UserSession
    @ParameterizedTest
    @Browsers({"firefox"})
    @MethodSource("invalidSums")
    public void userCantMakeDepositWithInvalidSum(Double sum, String alterMessage) {
        /*
        ### Тест: При сумме 5001 ошибка
Результат: ❌ Please deposit less or equal to 5000$.(Запроса к апи нет, проверка на фронте)


### Тест: При сумме 0 появляется ошибка
Результат: ❌ Please enter a valid amount. (Запроса к апи нет, проверка на фронте)
         */

        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(SessionStorage.getUser().getRequest());

        new MakeDeposit()
                .openDepositSection()
                .selectAccount(accountResponse.getAccountNumber())
                .enterAmount(sum)
                .clickTheDepositButton()
                .checkAlertMessageAndAccept(alterMessage);
    }
}
