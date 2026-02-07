package ui.level2integration;

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

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TransferMoney2Test extends BaseUiTest {

    @Test
    public void userCanTransferMoneyToUserWithRecipientName() {
            /*
### Тест: юзер отправляет другому юзеру перевод с использованием имени юзера
Предшаги через апи: админ создает юзера 1 и юзера 2. Создаем им счета, юзеру 1 делаем депозит 5000. Юзер 2 имеет имя
Шаги UI: Юзер 1 логинится, переходит на Make a Transfer, выбирает свой счет, имя не вводить, вводит счет юзера 2, сумму меньше или равно 5000, выбирает чекбокс, нажимает Send Transfer
Результат: Алерт ✅ Successfully transferred $(сумма) to account (акк получателя)!. Юзер 1 останется на той же странице
Проверка через апи балансов получателя и отправителя
             */

        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX2(accountResponse1.getId(), user1.getRequest());
        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());


        CreatedUser user2 = AdminSteps.createUser();
        String recipientName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());


        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");

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

        //у юзера 1 убавился баланс
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        soflty.assertThat(user1BalanceAfter).isEqualTo(user1BalanceBefore - MaxSumsForDepositAndTransactions.TRANSACTION.getMax());

        //у юзера 2 прибавился баланс
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());
        soflty.assertThat(user2BalanceAfter).isEqualTo(user2BalanceBefore + MaxSumsForDepositAndTransactions.TRANSACTION.getMax());


        AdminSteps.deletesUser(user1.getRequest());
        AdminSteps.deletesUser(user2.getRequest());
    }

    @Test
    public void userCanTransferMoneyToUserIfRecipientDoesntHaveName() {
            /*

### Тест: юзер отправляет другому юзеру перевод без имени получателя
Предшаги через апи: админ создает юзера 1 и юзера 2. Создаем им счета, юзеру 1 делаем депозит 5000. Юзер 2 имеет имя
Шаги UI: Юзер 1 логинится, переходит на Make a Transfer, выбирает свой счет, имя не вводить, вводит счет юзера 2, сумму меньше или равно 5000, выбирает чекбокс, нажимает Send Transfer
Результат: Алерт ✅ Successfully transferred $(сумма) to account (акк получателя)!. Юзер 1 останется на той же странице
Проверка через апи балансов получателя и отправителя
             */

        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX2(accountResponse1.getId(), user1.getRequest());
        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());


        CreatedUser user2 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());


        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");

        //select your acc
        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse1.getAccountNumber())).shouldBe(visible).click();

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

        //у юзера 1 убавился баланс
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        soflty.assertThat(user1BalanceAfter).isEqualTo(user1BalanceBefore - MaxSumsForDepositAndTransactions.TRANSACTION.getMax());

        //у юзера 2 прибавился баланс
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());
        soflty.assertThat(user2BalanceAfter).isEqualTo(user2BalanceBefore + MaxSumsForDepositAndTransactions.TRANSACTION.getMax());
    }


    @Test
    public void userCantTransferMoneyMoreThanHas() {
       /*

### Тест: юзер не может отправить денег больше, чем есть на балансе

Предшаги через апи: админ создает юзера 1 и юзера 2. Оба имеют счета. Юзер1 делает депозит 5000.
Шаги: юзер логинится, переходит на Make a Transfer. Выбирает свой счет, имя получателя не вводить, ввести счет дебета, сумму больше чем на балансе и отправить
Результат:
❌ Error: Invalid transfer: insufficient funds or invalid accounts

Проверка через апи балансов получателя и отправителя
        */

        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDeposit(accountResponse1.getId(), user1.getRequest()); //пополнили на 5000 а переводим 10
        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());


        CreatedUser user2 = AdminSteps.createUser();
        String recipientName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");

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
        assertThat(alert.getText()).contains("❌ Error: Invalid transfer: insufficient funds or invalid accounts");


        //получим балансы после
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        //балансы не должны были поменяться
        soflty.assertThat(user1BalanceAfter).isEqualTo(user1BalanceBefore);
        soflty.assertThat(user2BalanceAfter).isEqualTo(user2BalanceBefore);
    }

    @Test
    public void userCantTransferMoneyByAccId() {
        /*
### Тест: юзер отправляет деньги на существующий id счета

Предшаги через апи: админ создает юзера 1 и юзера 2. Оба имеют счета. Юзер1 делает депозит 5000.
Шаги: юзер логинится, переходит на Make a Transfer. Выбирает свой счет, имя получателя не вводить, ввести счет, которого нет в системе, в Recipient Account Number: вставляет id счета юзера 2, ввести сумму меньше 5000 и отправить

Результат: ❌ No user found with this account number.
Проверка через апи баланса получателя, нет списаний
         */
        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX2(accountResponse1.getId(), user1.getRequest());
        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());


        CreatedUser user2 = AdminSteps.createUser();
        String recipientName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");

        //select your acc
        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse1.getAccountNumber())).shouldBe(visible).click();

        //recipientName
        SelenideElement recipientNamePlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNamePlaceholder.sendKeys(recipientName);

        //recipient account number
        SelenideElement recipientAccNumberPlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccNumberPlaceholder.sendKeys(accountResponse2.getId().toString()); //берем именно id

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
        assertThat(alert.getText()).contains("❌ No user found with this account number.");


        //получим балансы после
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        //балансы не должны были поменяться
        soflty.assertThat(user1BalanceAfter).isEqualTo(user1BalanceBefore);
        soflty.assertThat(user2BalanceAfter).isEqualTo(user2BalanceBefore);
    }

    @Test
    public void userCantTransferMoneyWithoutNameIfRecipientNameIsExisted() {
        /*
### Тест: Юзер-получатель имеет имя, но имя не указано при создании платежа
Предшаги через апи: админ создает юзера 1 и юзера 2. Создаем им счета, юзеру 1 делаем депозит 5000. Юзеру 2 добавлено имя
Шаги UI: Юзер 1 логинится, переходит на Make a Transfer, выбирает свой счет, вводит счет юзера 2, имя не писать сумму меньше или равно 5000, выбирает чекбокс, нажимает Send Transfer

Результат:❌ The recipient name does not match the registered name.
Проверка через апи баланса получателя, нет списаний и зачислений у получателя
         */

        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX2(accountResponse1.getId(), user1.getRequest());
        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());


        CreatedUser user2 = AdminSteps.createUser();
        String recipientName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");

        //select your acc
        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse1.getAccountNumber())).shouldBe(visible).click();

        //recipient account number
        SelenideElement recipientAccNumberPlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccNumberPlaceholder.sendKeys(accountResponse2.getAccountNumber().toString());

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
        assertThat(alert.getText()).contains("❌ The recipient name does not match the registered name.");


        //получим балансы после
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        //балансы не должны были поменяться
        soflty.assertThat(user1BalanceAfter).isEqualTo(user1BalanceBefore);
        soflty.assertThat(user2BalanceAfter).isEqualTo(user2BalanceBefore);

    }

    @Test
    public void userCantTransferMoneyToItsAcc() {
        /*

### Тест: Отправка самому себе на тот же счет
Предшаги через апи: админ создает юзера. Юзер создает счет и делает депозит 5000
Шаги: юзер логинится, переходит на Make a Transfer. Выбирает свой счет, имя получателя не вводить,
в Recipient Account Number: вставляет также свой счет (ACC...), вводит сумму меньше 5000 и отправить

Ошибка ✅ Successfully transferred $2 to account ACC5!, ошибка бэка!

         */

        CreatedUser user = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX2(accountResponse1.getId(), user.getRequest());
        Double userBalanceBefore = UserSteps.getBalance(user.getRequest(), accountResponse1.getId());

        authAsUserUi(user.getRequest());
        Selenide.open("/transfer");

        //select your acc
        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse1.getAccountNumber())).shouldBe(visible).click();

        //recipient account number
        SelenideElement recipientAccNumberPlaceholder = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccNumberPlaceholder.sendKeys(accountResponse1.getAccountNumber());

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
                .contains("account " + accountResponse1.getAccountNumber());
        alert.accept();  //ошибка бэка
        //получим баланс после
        Double userBalanceAfter = UserSteps.getBalance(user.getRequest(), accountResponse1.getId());
        //баланс не изменился
        soflty.assertThat(userBalanceAfter).isEqualTo(userBalanceBefore);

    }
}
