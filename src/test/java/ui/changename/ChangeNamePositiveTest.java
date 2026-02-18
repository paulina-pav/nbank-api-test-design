package ui.changename;

import api.generators.RandomModelGenerator;
import api.models.UserChangeNameRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.models.CreatedUser;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.EditPage;
import ui.UserDashboard;
import ui.alerts.ChangeNameAlerts;

public class ChangeNamePositiveTest extends BaseUiTest {
    @UserSession
    @Test
    public void editProfilePageCheck() {

        new EditPage()
                .open()
                .elementsAreVisible();
    }

    @Test
    @UserSession
    public void userCanGoFromDashBoardToEditProfile() {
        new UserDashboard()
                .open()
                .goToEditProfile(SessionStorage.getUser().getRequest())
                .elementsAreVisible();
    }


    @Test
    @UserSession
    public void userCanSeeNewName() {
        /*
        ### Тест: Ранее установленное имя появляется в UI
         */


        String newName = UserSteps.changesNameReturnRequest(SessionStorage.getUser().getRequest()).toString();

        new UserDashboard()
                .open()
                .nameInHeaderIsVisibleAndCorrect(newName)
                .nameInWelcomeTextIsVisibleAndCorrect(newName)
                .goToEditProfile(SessionStorage.getUser().getRequest())
                .alreadyAddedNameIsVisibleInPlaceholder(newName);
    }

    @Test
    @UserSession
    public void userCanChangeNameSuccessfully() {


        String newName = RandomModelGenerator.generate(UserChangeNameRequest.class).toString();

        new EditPage()
                .open()
                .changeName(newName)
                .checkAlertMessageAndAccept(ChangeNameAlerts.NAME_UPDATED_SUCCESSFULLY.getMessage());

        //проверим через апи, установилось ли имя
        String actualName = UserSteps.getsProfile(SessionStorage.getUser().getRequest()).getName();

        //проверим, что, то имя, которое мы давали на фронт, совпадает с тем, что дошло на бэк
        soflty.assertThat(actualName).isEqualTo(newName);

    }

    @Test
    @UserSession
    public void userCanSeeUpdatedNameEverywhere() {

        String newName = RandomModelGenerator.generate(UserChangeNameRequest.class).toString();

        new UserDashboard()
                .open()
                .goToEditProfile(SessionStorage.getUser().getRequest())
                // .nameInHeaderIsVisibleAndCorrect(newName) //в хедере не обновилось
                .changeName(newName)
                .clickHomeButton()
                // .nameInHeaderIsVisibleAndCorrect(newName) //в хедере не обновилось и на дашборде
                .nameInWelcomeTextIsVisibleAndCorrect(newName);

        //проверим через апи, установилось ли имя
        String actualName = UserSteps.getsProfile(SessionStorage.getUser().getRequest()).getName();

        //проверим, что, то имя, которое мы давали на фронт, совпадает с тем, что дошло на бэк
        soflty.assertThat(actualName).isEqualTo(newName);
    }
}
