package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;
import org.openqa.selenium.Alert;
import ui.elements.DepositSection;


import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MakeDeposit extends BasePage<MakeDeposit> {

    @Override
    public String url() {
        return "/deposit";
    }

    private SelenideElement title = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home"));
    private final SelenideElement depositSection = $("div.container.mt-4.text-center");


    public DepositSection openDepositSection() {
            open();
            depositSection.shouldBe(visible);
            return new DepositSection(depositSection, this);
    }


    public MakeDeposit checkIfTitleIsCorrect() {
        return StepLogger.logUi("User checks title", () -> {
            title.shouldBe(visible);
            return this;
        });
    }

    public UserDashboard clickHomeButton() {
        return StepLogger.logUi("User click the Home button", () -> {
            homeButton.click();
            return Selenide.page(UserDashboard.class);
        });
    }

    public UserDashboard checkAlertMessageAndAcceptAndGoToUserDashboard(String bankAlert) {
        return StepLogger.logUi("User accept alert and goes to the dashboard", () -> {
            Alert alert = switchTo().alert();
            assertThat(alert.getText()).contains(bankAlert);
            alert.accept();
            return Selenide.page(UserDashboard.class);
        });
    }
}
