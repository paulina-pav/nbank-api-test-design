package ui;

import com.codeborne.selenide.*;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MakeDeposit extends BasePage<MakeDeposit> {

    @Override
    public String url() {
        return "/deposit";
    }

    SelenideElement title = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));

    SelenideElement selectAccountText = $(Selectors.byText("Select Account:"));
    SelenideElement dropdownText = $(Selectors.byText("-- Choose an account --"));

    SelenideElement enterAmountText = $(Selectors.byText("Enter Amount:"));

    SelenideElement enterAmountPlaceholder = $(Selectors.byAttribute("placeholder", "Enter amount"));

    SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

    SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home"));




    public MakeDeposit elementsAreVisible() {
        title.shouldBe(visible);
        selectAccountText.shouldBe(visible);
        dropdownText.shouldBe(visible);
        enterAmountPlaceholder.shouldBe(visible);
        enterAmountText.shouldBe(visible);
        depositButton.shouldBe(visible);
        homeButton.shouldBe(visible);

        return this;
    }

    public MakeDeposit clickTheDepositButton() {
        depositButton.click();
        return this;
    }

    public MakeDeposit selectAccount(String accountNumber) {
        ElementsCollection allAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        allAccountsFromDropdown.findBy(Condition.text(accountNumber)).shouldBe(visible).click();

        return this;
    }

    public MakeDeposit enterAmount(Double amount) {
        String amountString = amount.toString();

        SelenideElement placeholderAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
        placeholderAmount.sendKeys(amountString);

        return this;
    }

    public UserDashboard clickHomeButton(){
        homeButton.click();
        return Selenide.page(UserDashboard.class);
    }

    public UserDashboard checkAlertMessageAndAcceptAndGoToUserDashboard(String bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert);
        alert.accept();
        return Selenide.page(UserDashboard.class);
    }
}
