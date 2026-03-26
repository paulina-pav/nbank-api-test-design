package ui.changename;

import api.generators.RandomModelGenerator;
import api.models.UserChangeNameRequest;
import api.requests.steps.UserSteps;
import common.annotation.Browsers;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.pages.EditPage;
import ui.pages.UserDashboard;
import ui.alerts.ChangeNameAlerts;

public class ChangeNamePositiveTest extends BaseUiTest {

    @Test
    @UserSession
    @Browsers({"firefox"})
    public void userCanGoFromDashBoardToEditProfile() {
        new UserDashboard()
                .open()
                .goToEditProfile(SessionStorage.getUser().getRequest())
                .elementsAreVisible();
    }


    @Test
    @UserSession
    @Browsers({"firefox"})
    public void userCanSeeNewName() {
        /*
        ### Тест: Ранее установленное имя появляется в UI
         */

        String newName = UserSteps.changesNameReturnRequest(SessionStorage.getUser().getRequest()).toString();

        new UserDashboard()
                .open()
                .nameInHeaderIsVisibleAndCorrect(newName)
                .nameInWelcomeTextIsVisibleAndCorrect(newName)
                .goToEditProfile(SessionStorage.getUser().getRequest())//нашли элемент по имени юзера
                .openEditNameSection()
                .alreadyAddedNameIsVisibleInPlaceholder(newName);
    }

    @Test
    @UserSession
    @Browsers({"firefox"})
    public void userCanChangeNameSuccessfully() {

        String newName = RandomModelGenerator.generate(UserChangeNameRequest.class).toString();

        new EditPage()
                .openEditNameSection()
                .enterNewName(newName)
                .clickSaveChangesButton()
                .checkAlertMessageAndAccept(ChangeNameAlerts.NAME_UPDATED_SUCCESSFULLY.getMessage());

        //проверим через апи, установилось ли имя
        String actualName = UserSteps.getsProfile(SessionStorage.getUser().getRequest()).getName();

        //проверим, что, то имя, которое мы давали на фронт, совпадает с тем, что дошло на бэк
        soflty.assertThat(actualName).isEqualTo(newName);

    }

    @Test
    @UserSession
    @Browsers({"firefox"})
    public void userCanSeeUpdatedNameEverywhere() {

        String newName = RandomModelGenerator.generate(UserChangeNameRequest.class).toString();

        new UserDashboard()
                .open()
                .goToEditProfile(SessionStorage.getUser().getRequest())
                .openEditNameSection()
                .enterNewName(newName)
                .clickSaveChangesButton()
                .checkAlertMessageAndAccept(ChangeNameAlerts.NAME_UPDATED_SUCCESSFULLY.getMessage())
                // .nameInHeaderIsVisibleAndCorrect(newName) //в хедере не обновилось
                .clickHomeButton()
                // .nameInHeaderIsVisibleAndCorrect(newName) //в хедере не обновилось и на дашборде
                .nameInWelcomeTextIsVisibleAndCorrect(newName);

        //проверим через апи, установилось ли имя
        String actualName = UserSteps.getsProfile(SessionStorage.getUser().getRequest()).getName();

        //проверим, что, то имя, которое мы давали на фронт, совпадает с тем, что дошло на бэк
        soflty.assertThat(actualName).isEqualTo(newName);
    }
}
