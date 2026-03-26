package ui.level1frontend;

import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.RandomModelGenerator;
import api.models.CreateAnAccountResponse;
import api.models.MakeDepositResponse;
import api.models.UserChangeNameRequest;
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

public class TransferMoneyTest1 extends BaseUiTest {
    @Test
    public void MakeTransferPageCheck() {
        /*
### Тест: страница Make a Transfer

* Хидер
* Цвет фона
* Заголовок (шрифт, цвет)
* Дропдаун Select Your Account: и текст внутри (цвет, состояния и тд)
* Recipient Name: предписанный текст, состояние и тд
* Recipient Account Number: предписанный текст, состояние и тд
* Amount: предписанный текст, состояние и тд
* чекбокс Confirm details are correct -- выключен
* кнопка Send Transfer -- цвет, текст, состояния
* есть кнопка Home
* есть кнопка Transfer Again (цвет, текст, состояния)
* есть кнопка New Transfer (цвет, текст, состояние)
         */

        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());
        Selenide.open("/transfer");

        //основ часть
        SelenideElement pageName = $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(visible);
        SelenideElement newTransferButton = $(Selectors.byText("\uD83C\uDD95 New Transfer")).shouldBe(visible);
        SelenideElement TransferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);

        SelenideElement selectYourAccountText = $(Selectors.byText("Select Your Account:")).shouldBe(visible);
        SelenideElement dropdownText = $(Selectors.byText("-- Choose an account --")).shouldBe(visible);

        SelenideElement recipientNameText = $(Selectors.byText("Recipient Name:")).shouldBe(visible);
        SelenideElement recipientNamePlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient name")).shouldBe(visible);

        SelenideElement recipientAccountNumberText = $(Selectors.byText("Recipient Account Number:")).shouldBe(visible);
        SelenideElement recipientAccountNumberPlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient account number")).shouldBe(visible);

        SelenideElement amountText = $(Selectors.byText("Amount:")).shouldBe(visible);
        SelenideElement amountTextPlaceholder = $(Selectors.byAttribute("placeholder", "Enter amount")).shouldBe(visible);

        SelenideElement confirmCheckboxText = $(Selectors.byText("Confirm details are correct")).shouldBe(visible);
        //потом разобраться, как проверить, что чекбокс пустой

        SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer")).shouldBe(visible);

        SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home")).shouldBe(visible);

        //header
        SelenideElement noName = $(Selectors.byText("Noname")).shouldBe(visible);
        SelenideElement username = $(Selectors.byText(user.getRequest().getUsername())).shouldBe(visible);
        SelenideElement logoutButton = $(Selectors.byText("\uD83D\uDEAA Logout")).shouldBe(visible);
        //SelenideElement brandNameInHeader = $(Selectors.byText("NoBugs Bank")).shouldBe(visible);

    }
    @Test
    public void userCanGoFromMakeTransferToDashboard() {
        /*
### Тест: Из Make a Transfer вернуться на дашборд

* Попасть на Make a Transfer
* Нажать Home -- переход на дашборд
         */
        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());
        Selenide.open("/transfer");

        SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home"));
        homeButton.click();

        SelenideElement dashboard = $(Selectors.byText("User Dashboard")).shouldBe(visible);

    }
    @Test
    public void userCanGoFromDashboardToMakeTransfer() {
        /*

### Тест: из дашборда в Make a Transfer (наверное, замокать бэк или вообще вынести в интеграцию. Но хочется проверить куда ведут кнопки просто)
* попасть на дашборд
* нажать Make a Transfer
         */

        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());
        Selenide.open("/dashboard");

        SelenideElement makeTransferMenuButton = $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(visible);
        makeTransferMenuButton.click();

        SelenideElement pageName = $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(visible);

    }
    @Test
    public void userCantTransferWithoutSenderAcc() {
        /*
        ##Тест: юзер не может отправить трансфер без счета отправителя

        Предшаги апи: создать юзера, создать счет, пополнить счет. Создать юзера 2, сделать ему имя, сделать счет
        UI: оказаться на странице трансфер, заполнить все поля, кроме счета отправителя.

        Валидация на уровне UI, запрос не уходит

         */

        CreatedUser user1 = createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), user1.getRequest());


        CreatedUser user2 = createUser();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        String user2Name = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();


        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");

        //recipientName
        SelenideElement recipientNamePlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNamePlaceholder.sendKeys(user2Name);

        //recipient account number
        SelenideElement recipientAccNumberPlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccNumberPlaceholder.sendKeys(accountResponse2.getAccountNumber());

        //amount
        SelenideElement amountTextPlaceholder = $(Selectors.byAttribute("placeholder", "Enter amount"));
        amountTextPlaceholder.sendKeys(makeDepositResponse.getBalance().toString());

        //confirm
        SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck"));
        checkbox.click();

        //send button
        SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        sendTransferButton.click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("❌ Please fill all fields and confirm.");
        alert.accept();

    }
    @Test
    public void userCantTransferMoneyWithoutRecipientAccNumberAndName() {

/*
### Тест: юзер не может отправить трансфер без счета получателя и его имени
* Есть юзер с положительным балансом на аккаунте. Создать юзера 2, сделать ему имя, сделать счет
* Юзер логинится, переходит на Make a Transfer. Выбирает свой счет, пишет сумму, ставит чекбокс, не ставит счета получателя и его имя
Результат: ❌ Please fill all fields and confirm. (ошибка от фронта, запрос не уходит)
 */

        CreatedUser user = createUser();

        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user.getRequest());

        authAsUserUi(user.getRequest());
        Selenide.open("/transfer");

        //select your acc
        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse.getAccountNumber())).shouldBe(visible).click();

        //enter amount
        SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
        enterAmount.sendKeys(MaxSumsForDepositAndTransactions.DEPOSIT.getMax().toString());

        //confirm
        SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck"));
        checkbox.click();

        //send button
        SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        sendTransferButton.click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("❌ Please fill all fields and confirm.");
        alert.accept();
    }
    @Test
    public void userCantTransferMoneyWithoutSum() {
        /*
        ### Тест: юзер не может отправить перевод без суммы
* Есть юзер с положительном балансом на аккаунте. Создать юзера 2, сделать ему имя, сделать счет
* Юзер логинится, переходит на Make a Transfer. Выбирает свой счет, имя получателя, счет получателя, чекбокс, без суммы
Результат: ❌ Please fill all fields and confirm.

Запрос не уходит, валидация на UI
         */

        CreatedUser user1 = createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), user1.getRequest());


        CreatedUser user2 = createUser();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        String user2Name = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();


        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");


        //select your acc
        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse.getAccountNumber())).shouldBe(visible).click();

        //recipientName
        SelenideElement recipientNamePlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNamePlaceholder.sendKeys(user2Name);

        //recipient account number
        SelenideElement recipientAccNumberPlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccNumberPlaceholder.sendKeys(accountResponse2.getAccountNumber());

        //confirm
        SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck"));
        checkbox.click();

        //send button
        SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        sendTransferButton.click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("❌ Please fill all fields and confirm.");
        alert.accept();

    }
    @Test
    public void userCantTransferWithoutCheckbox() {
        /*
        ### Тест: юзер не может сделать трансфер без чекбокса
* Есть юзер с положительном балансом на аккаунте. Создать юзера 2, сделать ему имя, сделать счет
* Юзер логинится, переходит на Make a Transfer. Заполнить все поля, кроме чекбокса
Результат: ❌ Please fill all fields and confirm.

Проверка на уровне UI
         */

        CreatedUser user1 = createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), user1.getRequest());


        CreatedUser user2 = createUser();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        String user2Name = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();


        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");


        //select your acc
        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse.getAccountNumber())).shouldBe(visible).click();

        //recipientName
        SelenideElement recipientNamePlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNamePlaceholder.sendKeys(user2Name);

        //recipient account number
        SelenideElement recipientAccNumberPlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccNumberPlaceholder.sendKeys(accountResponse2.getAccountNumber());

        //enter amount
        SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
        enterAmount.sendKeys(MaxSumsForDepositAndTransactions.DEPOSIT.getMax().toString());

        //send button
        SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        sendTransferButton.click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("❌ Please fill all fields and confirm.");
        alert.accept();

    }
    @Test
    public void userCantMakeTransferToItsAccId() {
        /*
        ### Тест: юзер не может сделать трансфер на свой id счета

Предшаги: Есть юзер с положительном балансом на аккаунте
Шаги: юзер логинится, переходит на Make a Transfer. Выбирает свой счет, имя получателя не вводить,
в Recipient Account Number вставляет id своего же счета, ввести сумму меньше 5000 и отправить

Результат: ❌ You cannot transfer money to the same account. Запрос не ушел
         */

        CreatedUser user1 = createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), user1.getRequest());

        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");


        //select your acc
        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse.getAccountNumber())).shouldBe(visible).click();

        //recipient account number
        SelenideElement recipientAccNumberPlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccNumberPlaceholder.sendKeys(accountResponse.getId().toString());

        //enter amount
        SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
        enterAmount.sendKeys(MaxSumsForDepositAndTransactions.DEPOSIT.getMax().toString());

        //confirm
        SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck"));
        checkbox.click();

        //send button
        SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        sendTransferButton.click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("❌ You cannot transfer money to the same account.");
        alert.accept();

    }

    @Test
    public void userCantTransferMoneyToUnknownAcc() {
        /*

### Тест: юзер отправляет деньги на свой id счета

Предшаги: Есть юзер с положительном балансом на аккаунте
Шаги: юзер логинится, переходит на Make a Transfer.
Выбирает свой счет, имя получателя не вводить, ввести счет, которого нет в системе, ввести сумму меньше 5000 и отправить

Результат: ❌ No user found with this account number. Запрос не ушел
         */

        CreatedUser user1 = createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), user1.getRequest());

        String unknownAcc = RandomModelGenerator.generate(UserChangeNameRequest.class).getName(); //сделала имя произвольной строкой

        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");


        //select your acc
        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse.getAccountNumber())).shouldBe(visible).click();

        //recipient account number
        SelenideElement recipientAccNumberPlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccNumberPlaceholder.sendKeys(unknownAcc);

        //enter amount
        SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
        enterAmount.sendKeys(MaxSumsForDepositAndTransactions.DEPOSIT.getMax().toString());

        //confirm
        SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck"));
        checkbox.click();

        //send button
        SelenideElement sendTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        sendTransferButton.click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("❌ No user found with this account number");
        alert.accept();

    }
    @Test
    public void userSeesChangedBalanceAfterTransferInAcc() {
        /*

### Тест: после отправки платежа в Select Your Account баланс обновляется

Предшаги: юзер 1 с положительном балансом на аккаунте, юзер 2 имеет счет и имя
Предшаги в UI: Юзер 1 логинится, переходит на Make a Transfer, выбирает свой счет, вводит счет и имя юзера 2, сумму меньше или равно 5000,
выбирает чекбокс, нажимает Send Transfer.

Шаг: После отправки сообщения юзер остается на странице Make a Transfer. В Select Your Account теперь предвыбран счет получателя,
но его баланс не изменился с учетом прошедшей транзакции

Результат: ошибка! баланс не совпадает с действительным -- фронтенд не обновил страницу динамически, запрос /accounts или  /users
         */


        CreatedUser user1 = createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), user1.getRequest());


        CreatedUser user2 = createUser();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        String user2Name = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();


        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");

        //select your acc
        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse.getAccountNumber())).shouldBe(visible).click();

        //recipientName
        SelenideElement recipientNamePlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNamePlaceholder.sendKeys(user2Name);

        //recipient account number
        SelenideElement recipientAccNumberPlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccNumberPlaceholder.sendKeys(accountResponse2.getAccountNumber());

        //enter amount
        SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
        enterAmount.sendKeys(MaxSumsForDepositAndTransactions.DEPOSIT.getMax().toString());

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
                .contains("$" + MaxSumsForDepositAndTransactions.DEPOSIT.getMax().toString())
                .contains("account " + accountResponse2.getAccountNumber());
        alert.accept();


        //ошибка: баланс остался такой же
        ElementsCollection myAccountsFromDropdownAfter = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(MaxSumsForDepositAndTransactions.DEPOSIT.getMax().toString())).shouldBe(visible);
    }
}
