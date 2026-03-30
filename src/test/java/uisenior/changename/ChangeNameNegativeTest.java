package uisenior.changename;

import api.requests.steps.UserSteps;
import common.annotation.Browsers;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uisenior.BaseUiTest;
import ui.pages.EditPage;
import ui.alerts.ChangeNameAlerts;

import java.util.stream.Stream;

public class ChangeNameNegativeTest extends BaseUiTest {
    @UserSession
    @Test
    @Browsers({"firefox"})
    public void userCantSaveEmptyChangeNameField() {

        new EditPage()
                .openEditNameSection()
                .clickSaveChangesButton()
                .checkAlertMessageAndAccept(ChangeNameAlerts.ENTER_VALID_NAME.getMessage());
    }


    public static Stream<Arguments> invalidName(){
        return Stream.of(
                Arguments.of("a", ChangeNameAlerts.NAME_CONTAINS_TWO_WORDS.getMessage())
        );
    }


    @UserSession
    @ParameterizedTest
    @MethodSource("invalidName")
    @Browsers({"firefox"})
    public void userCantInputInvalidName(String invalidName, String alertMessage) {

        new EditPage()
                .openEditNameSection()
                .enterNewName(invalidName)
                .clickSaveChangesButton()
                .checkAlertMessageAndAccept(alertMessage);

        //проверим, что имя все еще null
        String actualName = UserSteps.getsProfile(SessionStorage.getUser().getRequest()).getName();
        soflty.assertThat(actualName).isNull();
    }
}
