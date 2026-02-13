package ui.changename;

import api.generators.RandomModelGenerator;
import api.models.UserChangeNameRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.EditPage;
import ui.UserDashboard;
import ui.alerts.ChangeNameAlerts;

public class ChangeNamePositiveTest extends BaseUiTest {
    @Test
    public void editProfilePageCheck() {
        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());

        new EditPage()
                .open()
                .elementsAreVisible();
    }

    @Test
    public void userCanGoFromDashBoardToEditProfile() {

        CreatedUser user = createUser();

        authAsUserUi(user.getRequest());
        new UserDashboard()
                .open()
                .goToEditProfile(user.getRequest())
                .elementsAreVisible();
    }


    @Test
    public void userCanSeeNewName() {
        /*
        ### Тест: Ранее установленное имя появляется в UI
         */

        CreatedUser user = createUser();
        String newName = UserSteps.changesNameReturnRequest(user.getRequest()).toString();

        authAsUserUi(user.getRequest());

        new UserDashboard()
                .open()
                .nameInHeaderIsVisibleAndCorrect(newName)
                .nameInWelcomeTextIsVisibleAndCorrect(newName)
                .goToEditProfile(user.getRequest())
                .alreadyAddedNameIsVisibleInPlaceholder(newName);
    }

    @Test
    public void userCanChangeNameSuccessfully() {

        CreatedUser user = AdminSteps.createUser();
        String newName = RandomModelGenerator.generate(UserChangeNameRequest.class).toString();
        authAsUserUi(user.getRequest());


        new EditPage()
                .open()
                .changeName(newName)
                .checkAlertMessageAndAccept(ChangeNameAlerts.NAME_UPDATED_SUCCESSFULLY.getMessage());

        //проверим через апи, установилось ли имя
        String actualName = UserSteps.getsProfile(user.getRequest()).getName();

        //проверим, что, то имя, которое мы давали на фронт, совпадает с тем, что дошло на бэк
        soflty.assertThat(actualName).isEqualTo(newName);

    }

    @Test
    public void userCanSeeUpdatedNameEverywhere() {

        CreatedUser user = AdminSteps.createUser();
        String newName = RandomModelGenerator.generate(UserChangeNameRequest.class).toString();

        authAsUserUi(user.getRequest());


        new UserDashboard()
                .open()
                .goToEditProfile(user.getRequest())
                // .nameInHeaderIsVisibleAndCorrect(newName) //в хедере не обновилось
                .changeName(newName)
                .clickHomeButton()
                // .nameInHeaderIsVisibleAndCorrect(newName) //в хедере не обновилось
                .nameInWelcomeTextIsVisibleAndCorrect(newName);

        //проверим через апи, установилось ли имя
        String actualName = UserSteps.getsProfile(user.getRequest()).getName();

        //проверим, что, то имя, которое мы давали на фронт, совпадает с тем, что дошло на бэк
        soflty.assertThat(actualName).isEqualTo(newName);
    }
}
