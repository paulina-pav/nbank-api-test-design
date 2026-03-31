package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.Condition;
import ui.elements.TransferMoneyForm;


import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;


public class TransferMoneyPage extends BasePage<TransferMoneyPage> {
    private SelenideElement pageName = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));
    private SelenideElement newTransferButton = $(Selectors.byText("\uD83C\uDD95 New Transfer"));
    private SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again"));
    private SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home"));
    private final SelenideElement transferForm = $(Selectors.byClassName("form-group"));


    @Override
    public String url() {
        return "/transfer";
    }

    public TransferMoneyForm openTransferMoneyForm() {
        open();
        transferForm.shouldBe(visible);
        return new TransferMoneyForm(transferForm, this);
    }

    public TransferMoneyPage checkPageName() {
        pageName.shouldBe(visible);
        return this;
    }

    public TransferMoneyPage checkIfElementsAreVisible() {
        pageName.shouldBe(visible);
        newTransferButton.shouldBe(visible);
        transferForm.shouldBe(visible);
        homeButton.shouldBe(visible);
        transferAgainButton.shouldBe(visible);
        return this;
    }


    public UserDashboard clickHomeButtonToGoToDashboard() {
        homeButton.shouldBe(visible).click();
        return Selenide.page(UserDashboard.class);
    }

    public TransferAgainPage openTransferAgain() {
        transferAgainButton.shouldBe(visible).click();
        return Selenide.page(TransferAgainPage.class);
    }

    public TransferMoneyPage checkBalanceAfterSuccessTransaction(Double sum) {
        String sumString = sum.toString();
        SelenideElement dropdownAfter = transferForm
                .find(Selectors.byText("-- Choose an account --"))
                .parent();

        dropdownAfter.findAll("option")
                .findBy(Condition.text(sumString))
                .shouldBe(visible);

        return this;
    }
}
