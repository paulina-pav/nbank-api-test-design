package ui.level2integration;

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

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ChangeName2Test extends BaseUiTest {

    @Test
    public void userCanChangeNameSuccessfully() {
        /*
        ### Тест: юзер успешно меняет имя

Предшаги через апи: админ создает пользователя.
Предшаги в UI: Юзер залогинился, попал на страницу Edit profile
Шаг: Юзер вводит в текстовый плейсхолдер валидное имя
Результат: ✅ Name updated successfully!  (Сработал апи запрос, фронт интерпретировал код 200)
Результат: через апи запрос убедиться, что имя обновилось
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

        //проверим через апи, установилось ли имя
        String actualName = UserSteps.getsProfile(user.getRequest()).getName();

        //проверим, что, то имя, которое мы давали на фронт, совпадает с тем, что дошло на бэк
        soflty.assertThat(actualName).isEqualTo(newName);

    }

    @Test
    public void UserCanSeeNameOnDashboard() {
        /*
        ### Тест: Имя появляется в welcome-тексте
Предшаги через апи: админ создает пользователя, юзер сменил имя
Предшаги в UI: Юзер залогинился и видит на дашборд
Результат: В приветственный текст встало новое имя (отработал запрос profile и его правильно интерпретирован фронт)
         */

        CreatedUser user = AdminSteps.createUser();
        String name = UserSteps.changesNameReturnRequest(user.getRequest()).getName();

        authAsUserUi(user.getRequest());

        Selenide.open("/dashboard");

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, " + name + "!"));

    }

    @Test
    public void userCantInputInvalidName() {
        /*
        ### Тест: юзер вводит невалидное имя
Предшаги через апи: админ создает пользователя. Пользователь создает счет
Предшаги в UI: Юзер залогинился, попал на страницу Edit profile
Шаг: Юзер вводит в текстовый плейсхолдер невалидное имя
Результат: Name must contain two words with letters only (Сработал апи запрос, фронт интерпретировал код 400)
         */

        CreatedUser user = AdminSteps.createUser();

        authAsUserUi(user.getRequest());
        Selenide.open("/edit-profile");

        SelenideElement placeholderEnterNewName = $(Selectors.byAttribute("placeholder", "Enter new name"));
        placeholderEnterNewName.sendKeys("a");

        SelenideElement buttonSaveChanges = $(Selectors.byText("\uD83D\uDCBE Save Changes"));
        buttonSaveChanges.click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("Name must contain two words with letters only");
        alert.accept();

        //проверим, что имя все еще null
        String actualName = UserSteps.getsProfile(user.getRequest()).getName();
        soflty.assertThat(actualName).isNull();
    }
}
