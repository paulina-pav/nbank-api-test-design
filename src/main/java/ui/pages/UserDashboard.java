package ui.pages;

import api.models.NewUserRequest;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;
import lombok.Getter;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;


@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    private SelenideElement title = $(Selectors.byText("User Dashboard"));
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement nameInHeader = $(".user-info .user-name");
    private SelenideElement depositMoneyButton = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement makeTransferMenuButton = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));


    @Override
    public String url() {
        return "/dashboard";
    }

    public TransferMoneyPage clickTransferMoneyButton() {
        return StepLogger.logUi("User clicks transfer money button", () -> {
            makeTransferMenuButton.click();
            return Selenide.page(TransferMoneyPage.class);
        });
    }

    public UserDashboard elementsAreVisible() {
        return StepLogger.logUi("User checks elemets", () -> {
            title.shouldBe(visible);
            welcomeText.shouldBe(visible);
            createNewAccount.shouldBe(visible);
            nameInHeader.shouldBe(visible);
            depositMoneyButton.shouldBe(visible);
            return this;
        });
    }

    public UserDashboard createNewAccount() {
        return StepLogger.logUi("User creates new acc", () -> {
            createNewAccount.click();
            return this;
        });
    }

    public EditPage goToEditProfile(NewUserRequest user) {
        return StepLogger.logUi("User opens edit profile page", () -> {
            SelenideElement editProfileButton = $(Selectors.byText(user.getUsername()));

            editProfileButton.click();
            return Selenide.page(EditPage.class);
        });
    }

    public UserDashboard nameInHeaderIsVisibleAndCorrect(String name) {
        return StepLogger.logUi("User checks name", () -> {
            nameInHeader.shouldHave(exactText(name));
            return this;
        });
    }

    public UserDashboard nameInWelcomeTextIsVisibleAndCorrect(String name) {
        return StepLogger.logUi("User checks welcome text", () -> {
            SelenideElement nameInWelcometext = $(Selectors.byClassName("welcome-text"))
                    .shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, " + name + "!"));
            return this;
        });
    }


    public MakeDeposit clickMakeDepositButton() {
            depositMoneyButton.click();
            return Selenide.page(MakeDeposit.class);
    }
}
