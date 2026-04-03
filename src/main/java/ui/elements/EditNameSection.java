package ui.elements;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.helpers.AllureAttachments;
import common.helpers.StepLogger;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import ui.pages.EditPage;

import static com.codeborne.selenide.Condition.*;


public class EditNameSection extends BaseElement {

    private final EditPage page;

    public EditNameSection(SelenideElement element, EditPage page) {
        super(element);
        this.page = page;
    }

    @Step("Юзер вводит имя")
    public EditNameSection enterNewName(String newName) {


        SelenideElement nameInput = find(
                Selectors.byAttribute("placeholder", "Enter new name")
        ).shouldBe(visible, enabled);

        nameInput.click();
        nameInput.clear();
        nameInput.setValue(newName);

       // nameInput.shouldHave(value(newName));

        return this;


       // AllureAttachments.attachScreenshot("entered name");

    }

    @Step("Юзер нажал на кнопку Save changes")
    public EditPage clickSaveChangesButton() {
        find((Selectors.byText("\uD83D\uDCBE Save Changes"))).shouldBe(visible).click();
        return page;
    }

    public EditNameSection alreadyAddedNameIsVisibleInPlaceholder(String name) {
        return StepLogger.logUi("User sees updated name", () -> {
            find((Selectors.byAttribute("placeholder", "Enter new name")))
                    .shouldBe(visible)
                    .shouldHave(Condition.attribute("value", name));
            return this;
        });
    }
}
