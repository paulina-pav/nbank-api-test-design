package ui.elements;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.helpers.AllureAttachments;
import common.helpers.StepLogger;
import lombok.Getter;
import ui.pages.TransferMoneyPage;

import static com.codeborne.selenide.Condition.visible;


@Getter
public class TransferMoneyForm extends BaseElement {

    private final TransferMoneyPage page;


    public TransferMoneyForm(SelenideElement element, TransferMoneyPage page) {
        super(element);
        this.page = page;
    }


    public TransferMoneyForm selectSenderAccount(String acc) {

        SelenideElement dropdown = find(Selectors.byText("-- Choose an account --")).parent().shouldBe(visible);
        dropdown.findAll("option").findBy(Condition.text(acc)).shouldBe(visible).click();
        AllureAttachments.attachScreenshot("selected");
        return this;

    }


    public TransferMoneyForm enterRecipientName(String name) {

        find(Selectors.byAttribute("placeholder", "Enter recipient name")).shouldBe(visible).sendKeys(name);
        AllureAttachments.attachScreenshot("entered");
        return this;

    }

    public TransferMoneyForm enterRecipientAccount(String acc) {

        find(Selectors.byAttribute("placeholder", "Enter recipient account number")).shouldBe(visible).sendKeys(acc);
        AllureAttachments.attachScreenshot("entered");
        return this;

    }

    public TransferMoneyForm enterAmount(Double sum) {

        String sumString = sum.toString();
        find(Selectors.byAttribute("placeholder", "Enter amount")).shouldBe(visible).sendKeys(sumString);
        AllureAttachments.attachScreenshot("entered");
        return this;

    }

    public TransferMoneyForm selectEmptyConfirmationCheckbox() {

            find(Selectors.byAttribute("id", "confirmCheck")).shouldBe(visible).click();
            return this;

    }

    public TransferMoneyPage submit() {
        find(Selectors.byText("\uD83D\uDE80 Send Transfer")).shouldBe(visible).click();
        return page;
    }
}
