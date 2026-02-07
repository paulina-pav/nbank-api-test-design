package ui.level3e2e;

import api.generators.MaxSumsForDepositAndTransactions;
import api.models.CreateAnAccountResponse;
import api.models.MakeDepositResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import com.codeborne.selenide.*;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import ui.BaseUiTest;

import static com.codeborne.selenide.Condition.exactValue;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TransferMoney3Test extends BaseUiTest {


    /*
    ## Этаж 3 - Юзер-сценарии

* e2e отправка денег от юзера к юзеру
* e2e отправка денег от юзера к юзеру, но не хватило денег
     */

    @Test
    public void userCanTransferMoneyToUser(){
        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX2(accountResponse1.getId(), user1.getRequest());
        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());


        CreatedUser user2 = AdminSteps.createUser();
        String recipientName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());


        authAsUserUi(user1.getRequest());
        Selenide.open("/dashboard");

        SelenideElement makeTransferMenuButton = $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(visible);
        makeTransferMenuButton.click();

        SelenideElement pageName = $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(visible);


        //select your acc
        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse1.getAccountNumber())).shouldBe(visible).click();

        //recipientName
        SelenideElement recipientNamePlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNamePlaceholder.sendKeys(recipientName);

        //recipient account number
        SelenideElement recipientAccNumberPlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccNumberPlaceholder.sendKeys(accountResponse2.getAccountNumber());

        //enter amount
        SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
        enterAmount.sendKeys(MaxSumsForDepositAndTransactions.TRANSACTION.getMax().toString());

        //confirm
        SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck"));
        checkbox.click();

        //send button
        SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        sendTransferButton.click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText)
                .contains("✅ Successfully transferred") //
                .contains("$" + MaxSumsForDepositAndTransactions.TRANSACTION.getMax().toString())
                .contains("account " + accountResponse2.getAccountNumber());
        alert.accept();


        //после успешного перевода остались на странице make a transfer, заполнен только счет отправителя (со старым балансом)

        //select your acc

        ElementsCollection myAccountsFromDropdownAfter = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdownAfter.findBy(Condition.text(accountResponse1.getAccountNumber())).shouldBe(visible);

        //recipientName
        SelenideElement recipientNamePlaceholderAfter = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNamePlaceholderAfter
                .shouldHave(exactValue(""));

        //recipient account number
        SelenideElement recipientAccNumberPlaceholderAfter = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccNumberPlaceholderAfter
                .shouldHave(exactValue(""));

        //enter amount
        SelenideElement enterAmountAfter = $(Selectors.byAttribute("placeholder", "Enter amount"));
        enterAmountAfter
                .shouldHave(exactValue(""));

        //у юзера 1 убавился баланс
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        soflty.assertThat(user1BalanceAfter).isEqualTo(user1BalanceBefore - MaxSumsForDepositAndTransactions.TRANSACTION.getMax());

        //у юзера 2 прибавился баланс
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());
        soflty.assertThat(user2BalanceAfter).isEqualTo(user2BalanceBefore + MaxSumsForDepositAndTransactions.TRANSACTION.getMax());
    }
}
