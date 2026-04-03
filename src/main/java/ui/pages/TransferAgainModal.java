package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.Selenide;
import common.helpers.AllureAttachments;
import common.helpers.StepLogger;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class TransferAgainModal extends BasePage<TransferAgainModal> {

    private SelenideElement modalHeader = $(Selectors.byText("\uD83D\uDD01 Repeat Transfer"));
    private SelenideElement confirmTransferToAccountText = $(".modal-body p")
            .shouldHave(text("Confirm transfer to Account ID:"));
    private SelenideElement selectAccountTest = $(Selectors.byText("Select Your Account:"));
    private ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --"))
            .parent().findAll("option");
    private SelenideElement amountText = $(Selectors.byText("Amount:"));
    private SelenideElement confirmationText = $(Selectors.byText("Confirm details are correct"));
    private SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck"));
    private SelenideElement buttonSendTransfer = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));


    @Override
    public String url() {
        return "";
    }

    public TransferAgainModal findConfirmationTextTransferToAccount(Long id) {
            confirmTransferToAccountText.shouldHave(text(id.toString())).shouldBe(visible);
            return this;
    }

    public TransferAgainModal selectYourAccount(String accNumber) {

        myAccountsFromDropdown.findBy(Condition.text(accNumber)).shouldBe(visible).click();
        AllureAttachments.attachScreenshot("selected acc");
        return this;


    }

    public TransferAgainModal insertAmount(Double sum) {

        Integer amount = sum.intValue();
        SelenideElement amountPlaceholder = $(Selectors.byAttribute("value", amount.toString()));
        AllureAttachments.attachScreenshot("inserted amount");
        return this;

    }

    public TransferAgainModal confirm() {
        checkbox.click();
        return this;

    }

    public TransferAgainPage sendTransfer() {
        buttonSendTransfer.click();
        return Selenide.page(TransferAgainPage.class);
    }
}
