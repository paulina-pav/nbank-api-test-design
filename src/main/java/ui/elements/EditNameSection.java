package ui.elements;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import ui.pages.EditPage;
import static com.codeborne.selenide.Condition.visible;


public class EditNameSection extends BaseElement {

    private final EditPage page;

    public EditNameSection(SelenideElement element, EditPage page) {
        super(element);
        this.page = page;
    }

    public EditNameSection enterNewName(String newName) {

        find((Selectors.byAttribute("placeholder", "Enter new name"))).shouldBe(visible).sendKeys(newName);
        return this;
    }

    public EditPage clickSaveChangesButton() {
        find((Selectors.byText("\uD83D\uDCBE Save Changes"))).shouldBe(visible).click();
        return page;
    }

    public EditNameSection alreadyAddedNameIsVisibleInPlaceholder(String name) {
        find((Selectors.byAttribute("placeholder", "Enter new name")))
                .shouldBe(visible)
                .shouldHave(Condition.attribute("value", name));
        return this;
    }
}
