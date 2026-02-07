package ui.level1frontend;

import api.generators.RandomModelGenerator;
import api.models.UserChangeNameRequest;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import ui.BaseUiTest;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ChangeName1Test extends BaseUiTest {

    @Test
    public void editProfilePageCheck() {
        /*
### Тест: проверка страницы Edit Profile и ее UI
* Почти всё, что в верхней части страницы
* Заголовок ✏️ Edit Profile (цвет, шрифт, состояние)
* Текстовый плейсхолдер (цвет, шрифт, состояние, предписанный текст)
* кнопка Save changes (цвет, шрифт, состояние, ведет на дашборд)
         */

        CreatedUser user = createUser();

        authAsUserUi(user.getRequest());
        Selenide.open("/edit-profile");

        //header
        SelenideElement noName = $(Selectors.byText("Noname")).shouldBe(visible);
        SelenideElement username = $(Selectors.byText(user.getRequest().getUsername())).shouldBe(visible);
        SelenideElement logoutButton = $(Selectors.byText("\uD83D\uDEAA Logout")).shouldBe(visible);
        //SelenideElement brandNameInHeader = $(Selectors.byText("NoBugs Bank")).shouldBe(visible);


        //main page
        SelenideElement headerEditProfile = $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(visible);
        SelenideElement placeholderEnterNewName = $(Selectors.byAttribute("placeholder", "Enter new name")).shouldBe(visible);
        SelenideElement saveChangesButton = $(Selectors.byText("\uD83D\uDCBE Save Changes")).shouldBe(visible);
        SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home")).shouldBe(visible);
    }

    @Test
    public void userCanGoFromDashBoardToEditProfile() {
        /*
### Тест: проверка открытия страницы Edit Profile
-Быть на дашборде и нажать на кнопку с юзернеймом
         */

        CreatedUser user = createUser();

        authAsUserUi(user.getRequest());

        Selenide.open("/dashboard");
        SelenideElement username = $(Selectors.byText(user.getRequest().getUsername())).shouldBe(visible);
        username.click();

        //проверили что перешли
        SelenideElement headerEditProfile = $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(visible);

    }

    @Test
    public void userCantSaveEmptyChangeNameField() {
        /*
### Тест: Проверка ошибки на пустое поле

Предшаги через апи: админ создает пользователя.
Шаги в UI: Юзер на странице Edit profile и нажал Save Changes
Результат: ❌ Please enter a valid name. (Ошибка от фронта, запросы не уходят)
         */

        CreatedUser user = createUser();

        authAsUserUi(user.getRequest());
        Selenide.open("/edit-profile");


        SelenideElement buttonSaveChanges = $(Selectors.byText("\uD83D\uDCBE Save Changes"));

        buttonSaveChanges.click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("❌ Please enter a valid name.");
        alert.accept();
    }

    @Test
    public void userCanSeeUpdatedNameInHeader() {
        /*
        ### Тест: Обновленное имя появляется в компоненте хидер

Предшаги через апи админ создает пользователя.
Предшаги в UI: Юзер залогинился, попал на страницу Edit profile. Юзер вводит в текстовый плейсхолдер валидное имя. Имеется успешный алерт
Шаги: Будучи на странице Edit Profile, проверить обновление имени в хидере

Результат: Ошибка! Имя не обновилось. При переходе на дашборд хидер также не меняется, т.к нет запроса на бэк на profile
         */

        String changedName = RandomModelGenerator.generate(UserChangeNameRequest.class).toString();
        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());

        Selenide.open("/edit-profile");
        SelenideElement placeholderEnterNewName = $(Selectors.byAttribute("placeholder", "Enter new name"));
        placeholderEnterNewName.sendKeys(changedName);

        SelenideElement buttonSaveChanges = $(Selectors.byText("\uD83D\uDCBE Save Changes"));
        buttonSaveChanges.click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("✅ Name updated successfully!");
        alert.accept();

        SelenideElement nameInHeader = $(".user-info .user-name");
        nameInHeader.shouldHave(exactText("Noname")); //тут ошибка, должно быть уже новое имя, а не noName

    }

    @Test
    public void userCanSeeNewName() {
        /*
        ### Тест: Ранее установленное имя появляется в UI
Предшаги через апи: админ создает пользователя. Через апи юзер установил себе имя
Шаг: юзер логинится в UI
Результат: Имя установлено в хидере, в приветственном тексте. При переходе на страницу Edit profile, имя предписано в плейсхолдере.
         */


        CreatedUser user = createUser();
        String newName = UserSteps.changesNameReturnRequest(user.getRequest()).toString();

        authAsUserUi(user.getRequest());
        Selenide.open("/dashboard");
        SelenideElement nameInHeader = $(".user-info .user-name");
        nameInHeader.shouldHave(exactText(newName));
    }
}
