package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;
import lombok.Getter;
import ui.elements.EditNameSection;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;


@Getter
public class EditPage extends BasePage<EditPage> {

    private SelenideElement headerEditProfile = $(Selectors.byText("✏\uFE0F Edit Profile"));
    private SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home"));
    private SelenideElement nameInHeader = $(".user-info .user-name");

    private final SelenideElement editPageSection = $("div.container.mt-5.text-center");


    @Override
    public String url() {
        return "/edit-profile";
    }


    public EditNameSection openEditNameSection() {
            open();
            editPageSection.shouldBe(visible);
            return new EditNameSection(editPageSection, this);
    }

    public EditPage elementsAreVisible() {
        return StepLogger.logUi("User sees the elements", () -> {
            headerEditProfile.shouldBe(visible);
            homeButton.shouldBe(visible);
            nameInHeader.shouldBe(visible);
            return this;
        });
    }

    public UserDashboard clickHomeButton() {
        return StepLogger.logUi("User is clicking the Home button", () -> {
            homeButton.click();
            return Selenide.page(UserDashboard.class);
        });
    }
}
