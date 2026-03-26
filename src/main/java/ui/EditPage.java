package ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;


@Getter
public class EditPage extends BasePage<EditPage> {

    private SelenideElement headerEditProfile = $(Selectors.byText("✏\uFE0F Edit Profile"));
    private SelenideElement placeholderEnterNewName = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));
    private SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home"));
    private SelenideElement nameInHeader = $(".user-info .user-name");

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditPage elementsAreVisible(){
        headerEditProfile.shouldBe(visible);
        placeholderEnterNewName.shouldBe(visible);
        saveChangesButton.shouldBe(visible);
        homeButton.shouldBe(visible);
        nameInHeader.shouldBe(visible);
        return this;
    }


    public EditPage changeName(String newName) {
        placeholderEnterNewName.sendKeys(newName);
        saveChangesButton.click();
        return this;
    }


    public EditPage nameInHeaderIsVisibleAndCorrect(String name) {
        nameInHeader.shouldHave(exactText(name));
        return this;
    }

    public EditPage clickSaveChangesButton() {
        saveChangesButton.click();
        return this;
    }

    public UserDashboard clickHomeButton() {
        homeButton.click();
        return Selenide.page(UserDashboard.class);
    }

    public EditPage alreadyAddedNameIsVisibleInPlaceholder(String name){
        placeholderEnterNewName.shouldHave(Condition.attribute("value", name));
        return this;
    }

}
