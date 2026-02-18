package ui.changename;

import api.requests.steps.UserSteps;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.EditPage;
import ui.alerts.ChangeNameAlerts;

public class ChangeNameNegativeTest extends BaseUiTest {
    @UserSession
    @Test
    public void userCantSaveEmptyChangeNameField() {

        new EditPage()
                .open()
                .clickSaveChangesButton()
                .checkAlertMessageAndAccept(ChangeNameAlerts.ENTER_VALID_NAME.getMessage());
    }

    @UserSession
    @Test
    public void userCantInputInvalidName() {

        new EditPage()
                .open()
                .changeName("a")
                .checkAlertMessageAndAccept(ChangeNameAlerts.NAME_CONTAINS_TWO_WORDS.getMessage());

        //проверим, что имя все еще null
        String actualName = UserSteps.getsProfile(SessionStorage.getUser().getRequest()).getName();
        soflty.assertThat(actualName).isNull();
    }
}
