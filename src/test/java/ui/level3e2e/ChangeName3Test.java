package ui.level3e2e;

import api.generators.RandomModelGenerator;
import api.models.UserChangeNameRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import ui.BaseUiTest;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ChangeName3Test extends BaseUiTest {

    @Test
    public void userChangesNameEverywhere() {
        /*
        ### Тест: Юзер меняет имя

* Через апи: создать юзера
UI:
* Юзер логинится и оказывается на дашборде
* Юзер нажимает на кнопку в хидере со своим логином
* Юзер вводит валидное имя
* Юзер отправляет его и получает положительный алерт
* Юзер жмет кнопку Home и оказывается на дашборде (компонент хидера не обновился), велком текст изменился
         */


        CreatedUser user = AdminSteps.createUser();

        authAsUserUi(user.getRequest());
        Selenide.open("/edit-profile");

        String newName = RandomModelGenerator.generate(UserChangeNameRequest.class).toString();

        SelenideElement placeholderEnterNewName = $(Selectors.byAttribute("placeholder", "Enter new name"));
        placeholderEnterNewName.sendKeys(newName);

        SelenideElement buttonSaveChanges = $(Selectors.byText("\uD83D\uDCBE Save Changes"));
        buttonSaveChanges.click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("✅ Name updated successfully!");
        alert.accept();

        //в хидере будучи на той же странице имя не установилось
        //сходить в дашборд: на дашборде установилось, в хидере тоже
        Selenide.open("/dashboard");

        SelenideElement nameInHeader = $(".user-info .user-name");
        nameInHeader.shouldHave(exactText(newName));

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, " + newName + "!"));


        //проверим через апи, установилось ли имя
        String actualName = UserSteps.getsProfile(user.getRequest()).getName();

        //проверим, что, то имя, которое мы давали на фронт, совпадает с тем, что дошло на бэк
        soflty.assertThat(actualName).isEqualTo(newName);
    }
}
