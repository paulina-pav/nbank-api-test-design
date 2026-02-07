package ui.level1frontend;

import api.models.CreateAnAccountResponse;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import com.codeborne.selenide.*;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import ui.BaseUiTest;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserMakesDeposit1Test extends BaseUiTest {

    @Test
    public void depositMoneyPageCheck() {
        /*
### Тест: проверка страницы Депозит и ее UI

* Все что в хедере
* Текст 💰 Deposit Money (цвет, шрифт, состояние)
* Дропдаун Select Account: (цвет, шрифт, состояние)
* Числовой плейсхолдер Enter Amount: (цвет, шрифт, состояние)
* Кнопка deposit (цвет, шрифт, состояния)
* Кнопка home (цвет, шрифт, состояния, ведет на дашборд)
         */

        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());
        Selenide.open("/deposit");

        //основная часть страницы
        SelenideElement pageName = $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(visible);
        SelenideElement selectAccountText = $(Selectors.byText("Select Account:")).shouldBe(visible);
        SelenideElement dropdownText = $(Selectors.byText("-- Choose an account --")).shouldBe(visible);
        SelenideElement enterAmountText = $(Selectors.byText("Enter Amount:")).shouldBe(visible);
        SelenideElement enterAmountPlaceholder = $(Selectors.byAttribute("placeholder", "Enter amount")).shouldBe(visible);
        SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit")).shouldBe(visible);

        SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home")).shouldBe(visible);

        //header
        SelenideElement noName = $(Selectors.byText("Noname")).shouldBe(visible);
        SelenideElement username = $(Selectors.byText(user.getRequest().getUsername())).shouldBe(visible);
        SelenideElement logoutButton = $(Selectors.byText("\uD83D\uDEAA Logout")).shouldBe(visible);
        //SelenideElement brandNameInHeader = $(Selectors.byText("NoBugs Bank")).shouldBe(visible);

    }

    @Test
    public void userCanGoFromDashboardToDepositMoneyPage(){
    /*
### Тест: проверка открытия страницы Депозит
* На страницу можно попасть из дашборда
     */

        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());
        Selenide.open("/dashboard");

        SelenideElement depositMoneyButton = $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(visible);
        depositMoneyButton.click();

        //проверим, что перешли
        SelenideElement pageName = $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(visible);
    }


    @Test
    public void userCantMakeDepositIfDoesntChooseAcc() {
        /*
### Тест: При невыбранном счете появляется ошибка

Предшаги через апи: админ создает пользователя
Предшаги в UI: Юзер залогинился и на странице Deposit Money. Нажимает кнопку Deposit
Результат: ❌ Please select an account. (Запроса к апи нет, проверка на фронте)
         */
        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());
        Selenide.open("/deposit");

        SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));
        depositButton.click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("❌ Please select an account.");
        alert.accept();
    }

    @Test
    public void userCantMakeDepositWithInvalidSum() {

        /*
        ### Тест: При сумме 5001 ошибка

Предшаги через апи: админ создает пользователя. Пользователь создает счет
Предшаги в UI: Юзер залогинился, на странице Deposit. Юзер выбирает свой счет, но сумма 5001. Нажать Deposit
Результат: ❌ Please deposit less or equal to 5000$.(Запроса к апи нет, проверка на фронте)
         */

        CreatedUser user = createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user.getRequest());

        authAsUserUi(user.getRequest());

        Selenide.open("/deposit");

        ElementsCollection allAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        allAccountsFromDropdown.findBy(Condition.text(accountResponse.getAccountNumber())).shouldBe(visible).click();


        SelenideElement placeholderAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
        placeholderAmount.sendKeys("5001");

        SelenideElement buttonDeposit = $(Selectors.byText("\uD83D\uDCB5 Deposit"));
        buttonDeposit.click();


        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("❌ Please deposit less or equal to 5000$.");
        alert.accept();
    }


    @Test
    public void userCantMakeDepositWitZero() {

        /*
### Тест: При сумме 0 появляется ошибка
Предшаги через апи: админ создает пользователя. Пользователь создает счет
Предшаги в UI: Юзер залогинился, попал на Deposit Money. Юзер выбирает свой счет, но сумма 0. Нажать Deposit
Результат: ❌ Please enter a valid amount. (Запроса к апи нет, проверка на фронте)
         */

        CreatedUser user = createUser();

        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user.getRequest());

        authAsUserUi(user.getRequest());
        Selenide.open("/deposit");
        //уже авторизованы и сразу можем идти на нужную страницу
        Selenide.open("/deposit");


        ElementsCollection allAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        allAccountsFromDropdown.findBy(Condition.text(accountResponse.getAccountNumber())).shouldBe(visible).click();


        SelenideElement placeholderAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
        placeholderAmount.sendKeys("0");

        SelenideElement buttonDeposit = $(Selectors.byText("\uD83D\uDCB5 Deposit"));
        buttonDeposit.click();


        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("❌ Please enter a valid amount.");
        alert.accept();
    }
}
