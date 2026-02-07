package ui.level3e2e;

import api.generators.MaxSumsForDepositAndTransactions;
import api.models.CreateAnAccountResponse;
import api.models.NewUserRequest;
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
    public void userMakesDeposit() {
        /*
    ## Этаж 3. e2e
### Тест: Юзер делает депозит
* Через апи: админ создал юзера
* UI:
- Юзер залогинился, попал на дашборд
- Нажать кнопку Deposit Money
- Перешел на страницу с депозитом
- выбрал счет, ввел валидную сумму
- Нажал депозит
- увидел алерт
- произошел переход на дашборд
     */

        CreatedUser user = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user.getRequest());
        //зафиксируем баланс ДО
        Double balanceBefore = UserSteps.getBalance(user.getRequest(), accountResponse.getId());

        authAsUserUi(user.getRequest());
        Selenide.open("/dashboard");

        SelenideElement depositMoneyButton = $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(visible);
        depositMoneyButton.click();

        //проверим, что перешли
        SelenideElement pageName = $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(visible);

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


        //автоматом переход на дашборд
//проверим, что он произошел
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible);

        //балансн на уровне апи ПОСЛЕ
        Double balanceAfter = UserSteps.getBalance(user.getRequest(), accountResponse.getId());

        //сверим, что баланс на уровне апи = сумме на которую делали депозит на UI
        soflty.assertThat(balanceAfter).isEqualTo(MaxSumsForDepositAndTransactions.DEPOSIT.getMax());
    }
}
