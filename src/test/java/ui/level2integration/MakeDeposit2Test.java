package ui.level2integration;

import api.generators.MaxSumsForDepositAndTransactions;
import api.models.CreateAnAccountResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import com.codeborne.selenide.*;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import ui.BaseUiTest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MakeDeposit2Test extends BaseUiTest {

    @Test
    public void userCanMakeDeposit() {
        /*
        ### Тест: юзер делает успешный депозит
Предшаги через апи: админ создает пользователя. Пользователь создает счет
Предшаги в UI: Юзер залогинился и попал на страницу депозита
Шаги: выбрать искомый счет, вписать валидную сумму и нажать Deposit
Результат: ✅ Successfully deposited $(искомая сумма) to account (искомый акк)!
Результат: через апи убедиться, что нужная сумма попала на нужный счет
         */

        CreatedUser user = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user.getRequest());

        //зафиксируем баланс ДО
        Double balanceBefore = UserSteps.getBalance(user.getRequest(), accountResponse.getId());

        authAsUserUi(user.getRequest());
        Selenide.open("/deposit");

        ElementsCollection allAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        allAccountsFromDropdown.findBy(Condition.text(accountResponse.getAccountNumber())).shouldBe(visible).click();

        SelenideElement placeholderAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
        placeholderAmount.sendKeys(MaxSumsForDepositAndTransactions.DEPOSIT.getMax().toString());

        SelenideElement buttonDeposit = $(Selectors.byText("\uD83D\uDCB5 Deposit"));
        buttonDeposit.click();

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Successfully deposited");

        Pattern pattern = Pattern.compile(
                "Successfully deposited \\$" +
                        Pattern.quote(MaxSumsForDepositAndTransactions.DEPOSIT.getMax().toString()) +
                        " to account " +
                        Pattern.quote(accountResponse.getAccountNumber()) +
                        "!"
        );
        Matcher matcher = pattern.matcher(alertText);
        matcher.find();
        alert.accept();

        //баланс на уровне апи ПОСЛЕ
        Double balanceAfter = UserSteps.getBalance(user.getRequest(), accountResponse.getId());

        //сверим, что баланс на уровне апи = сумме на которую делали депозит на UI
        soflty.assertThat(balanceAfter).isEqualTo(MaxSumsForDepositAndTransactions.DEPOSIT.getMax());
    }
}
