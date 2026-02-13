package ui.changename;

import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.EditPage;
import ui.alerts.ChangeNameAlerts;

public class ChangeNameNegativeTest extends BaseUiTest {
    @Test
    public void userCantSaveEmptyChangeNameField() {
        CreatedUser user = createUser();

        authAsUserUi(user.getRequest());
        new EditPage()
                .open()
                .clickSaveChangesButton()
                .checkAlertMessageAndAccept(ChangeNameAlerts.ENTER_VALID_NAME.getMessage());
    }
    @Test
    public void userCantInputInvalidName() {

        CreatedUser user = AdminSteps.createUser();

        authAsUserUi(user.getRequest());

        new EditPage()
                .open()
                .changeName("a")
                .checkAlertMessageAndAccept(ChangeNameAlerts.NAME_CONTAINS_TWO_WORDS.getMessage());

        //проверим, что имя все еще null
        String actualName = UserSteps.getsProfile(user.getRequest()).getName();
        soflty.assertThat(actualName).isNull();
    }
}
