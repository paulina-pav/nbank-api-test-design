package ui;

import com.codeborne.selenide.*;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class TransferMoneyPage extends BasePage<TransferMoneyPage> {
    private SelenideElement pageName = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));
    private SelenideElement newTransferButton = $(Selectors.byText("\uD83C\uDD95 New Transfer"));
    private SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again"));

    private SelenideElement selectYourAccountText = $(Selectors.byText("Select Your Account:"));
    private SelenideElement dropdownText = $(Selectors.byText("-- Choose an account --"));



    private SelenideElement recipientNameText = $(Selectors.byText("Recipient Name:"));
    private SelenideElement recipientNamePlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient name"));

    private SelenideElement recipientAccountNumberText = $(Selectors.byText("Recipient Account Number:"));
    private SelenideElement recipientAccountNumberPlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));

    private SelenideElement amountText = $(Selectors.byText("Amount:"));
    private SelenideElement amountTextPlaceholder = $(Selectors.byAttribute("placeholder", "Enter amount"));

    private SelenideElement confirmCheckboxText = $(Selectors.byText("Confirm details are correct"));

    private SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck"));
    //потом разобраться, как проверить, что чекбокс пустой

    private SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));

    private SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home"));

    private ElementsCollection myAccountsFromDropdownAfter = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");



    @Override
    public String url() {
        return "/transfer";
    }

    public TransferMoneyPage elementsAreVisible(){
        pageName.shouldBe(visible);
        newTransferButton.shouldBe(visible);
        transferAgainButton.shouldBe(visible);
        selectYourAccountText.shouldBe(visible);
        dropdownText.shouldBe(visible);
        recipientNameText.shouldBe(visible);
        recipientNamePlaceholder.shouldBe(visible);
        recipientAccountNumberText.shouldBe(visible);
        recipientAccountNumberPlaceholder.shouldBe(visible);
        amountText.shouldBe(visible);
        amountTextPlaceholder.shouldBe(visible);
        confirmCheckboxText.shouldBe(visible);
        sendTransferButton.shouldBe(visible);
        homeButton.shouldBe(visible);
        return this;
    }

    public void checkBalanceAfterSuccessTransaction(Double sum){
        myAccountsFromDropdownAfter.findBy(Condition.text(sum.toString())).shouldBe(visible);
    }



    public UserDashboard clickHomeButtonToGoToDashboard(){
        homeButton.click();
        return Selenide.page(UserDashboard.class);
    }

    public TransferMoneyPage selectSenderAccount(String acc){
        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(acc)).shouldBe(visible).click();
        return this;
    }


    public TransferMoneyPage enterRecipientName(String name){
        recipientNamePlaceholder.sendKeys(name);
        return this;
    }
    public TransferMoneyPage enterRecipientAccount(String acc){

        recipientAccountNumberPlaceholder.sendKeys(acc);
        return this;
    }

    public TransferMoneyPage enterAmount(Double sum){
        String sumString = sum.toString();

        amountTextPlaceholder.sendKeys(sumString);

        return this;
    }

    public TransferMoneyPage selectEmptyConfirmationCheckbox(){

        checkbox.click();

        return this;
    }

    public TransferMoneyPage clickTransferMoneyButton(){
        sendTransferButton.click();
        return this;
    }

    public UserDashboard clickHomeButton(){
        homeButton.click();
        return Selenide.page(UserDashboard.class);
    }

    public TransferAgainPage openTransferAgain(){
        transferAgainButton.click();
        return Selenide.page(TransferAgainPage.class);
    }


}
