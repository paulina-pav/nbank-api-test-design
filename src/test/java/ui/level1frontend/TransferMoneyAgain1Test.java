package ui.level1frontend;

import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.TransactionType;
import api.models.CreateAnAccountResponse;
import api.models.MakeDepositResponse;
import api.models.TransferMoneyResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import com.codeborne.selenide.*;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class TransferMoneyAgain1Test extends BaseUiTest {
    @Test
    public void transferAgainCheckPage() {
        /*
### Тест: Страница Transfer Again

* Хидер
* Заголовок (шрифт, цвет)
* есть кнопка Transfer Again (цвет, текст, состояния)
* есть кнопка New Transfer (цвет, текст, состояние)
* плейсхолдер Search by Username or Name: -- внутренний текст, состояния, цвет
* кнопка Search Transactions (цвет, текст, состояния)
* подзаголовок Matching Transactions (цвет, текст, шрифт)
* есть кнопка Home
         */

        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());
        Selenide.open("/transfer");

        //основная часть страницы

        SelenideElement pageName = $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(visible);
        SelenideElement newTransferButton = $(Selectors.byText("\uD83C\uDD95 New Transfer")).shouldBe(visible);
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);
        transferAgainButton.click();


        SelenideElement searchText = $(Selectors.byText("Search by Username or Name:")).shouldBe(visible);
        SelenideElement enterNameToFindPlaceholder = $(Selectors.byAttribute("placeholder", "Enter name to find transactions")).shouldBe(visible);

        SelenideElement searchTransactionsButton = $(Selectors.byText("\uD83D\uDD0D Search Transactions")).shouldBe(visible);
        SelenideElement matchingTransactionHeader = $(Selectors.byText("Matching Transactions")).shouldBe(visible);

        SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home")).shouldBe(visible);

        //header
        SelenideElement noName = $(Selectors.byText("Noname")).shouldBe(visible);
        SelenideElement username = $(Selectors.byText(user.getRequest().getUsername())).shouldBe(visible);
        SelenideElement logoutButton = $(Selectors.byText("\uD83D\uDEAA Logout")).shouldBe(visible);
        //SelenideElement brandNameInHeader = $(Selectors.byText("NoBugs Bank")).shouldBe(visible);
    }

    @Test
    public void userCanGoFromTransferAgainToNewTransfer() {
        /*

### Тест: Из Transfer Again в New Transfer

* Зайти на Make a Transfer
* Нажать Transfer Again
* Нажать New Transfer -- будет переход на Make a Transfer
         */
        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());
        Selenide.open("/transfer");

        SelenideElement newTransferButton = $(Selectors.byText("\uD83C\uDD95 New Transfer")).shouldBe(visible);
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);
        transferAgainButton.click();
        newTransferButton.click();

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

    }

    @Test
    public void userCanGoFromTransferAgainToDashboard() {
        /*

### Тест: Из Transfer Again в дашборд
* Зайти на Make a Transfer
* Нажать Transfer Again
* Нажать home -- переход на дашборд
         */

        CreatedUser user = createUser();
        authAsUserUi(user.getRequest());
        Selenide.open("/transfer");

        SelenideElement newTransferButton = $(Selectors.byText("\uD83C\uDD95 New Transfer")).shouldBe(visible);
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);
        transferAgainButton.click();

        SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home")).shouldBe(visible);
        homeButton.click();

        SelenideElement userDashboard = $(Selectors.byText("User Dashboard")).shouldBe(visible);

    }

    @Test
    public void userCanSeeTransferInTransaction() {
        /*
### Тест: у получателя имеется транзакция transfer in и он ее видит

API: Есть 2 юзера, один отправил деньги другому
UI: у юзер-получателя в Transfer Again есть транзакция Transfer In на искомую сумму
         */

        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());

        MakeDepositResponse makeDepositResponse = UserSteps.makesDepositX2(accountResponse.getId(), user1.getRequest());
        CreatedUser user2 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        TransferMoneyResponse transferMoneyResponse = UserSteps.transferMoney(accountResponse.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest());

        authAsUserUi(user2.getRequest());
        Selenide.open("/transfer");
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);
        transferAgainButton.click();

        ElementsCollection matchingTransaction = $(Selectors.byClassName("list-group")).findAll("li");
        matchingTransaction.findBy(Condition.text(TransactionType.TRANSFER_IN.getMessage() + "\n - $" + "\n" + transferMoneyResponse.getAmount()));
    }

    @Test
    public void userCanSeeTransferOutTransaction() {
        /*

### Тест: у юзера-отправителя имеется транзакция transfer out
API: Есть 2 юзера, один отправил деньги другому
UI: юзер-отправитель в Transfer Again есть транзакция Transfer out на искомую сумму
         */


        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDepositX2(accountResponse.getId(), user1.getRequest());
        CreatedUser user2 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        TransferMoneyResponse transferMoneyResponse = UserSteps.transferMoney(accountResponse.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest());


        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);
        transferAgainButton.click();

        ElementsCollection matchingTransaction = $(Selectors.byClassName("list-group")).findAll("li");
        matchingTransaction.findBy(Condition.text(TransactionType.TRANSFER_OUT.getMessage() + "\n - $" + "\n" + transferMoneyResponse.getAmount()));

    }

    @Test
    public void userCanSeeDepositTransaction() {
        /*

### Тест: Юзер видит транзакцию депозита
API: Есть юзер и он сделал депозит
UI: юзер в Transfer Again есть транзакция deposit на искомую сумму
         */

        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), user1.getRequest());

        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);
        transferAgainButton.click();

        ElementsCollection matchingTransaction = $(Selectors.byClassName("list-group")).findAll("li");
        matchingTransaction.findBy(Condition.text(TransactionType.DEPOSIT.getMessage() + "\n - $" + "\n" + makeDepositResponse.getBalance()));

    }

    @Test
    public void userCanSearchTransactionByRecipientName() {
        /*

### Тест: Юзер-отправитель может найти транзакцию по имени получателя в в разделе Transfer Again

Предшаги через апи: есть юзер 1 и юзер 2. У них есть счета, у юзера 1 баланс 5000. Юзер 2 имеет имя. Юзер 1 сделал перевод юзеру 2.

Шаг: юзер 1 логинится и жмет кнопку Make a Transfer, а затем Transfer Again. Имеется трансфер аут на искому сумму. Ввести в поле ввода юзернейм юзера 2.

Ошибка! при поиске находится транзакция трансфер ин -- я вижу транзакции своего получателя! UI не так парсит респонз /users
Потенциальная проблема: запрос не динамический, если ты в другом окне сделал перевод, то в этом он не появится, потому что страница не обновляется в процессе

         */

        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDepositX2(accountResponse.getId(), user1.getRequest());


        CreatedUser user2 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        String user2Name = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();

        TransferMoneyResponse transferMoneyResponse = UserSteps.transferMoney(accountResponse.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest());


        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);
        transferAgainButton.click();

        SelenideElement enterNameToFindPlaceholder = $(Selectors.byAttribute("placeholder", "Enter name to find transactions"));
        SelenideElement searchTransactionsButton = $(Selectors.byText("\uD83D\uDD0D Search Transactions"));

        enterNameToFindPlaceholder.sendKeys(user2Name);
        searchTransactionsButton.click();


        //Ошибка:транзакция должна быть трансфер аут, тк мы смотрим у юзера-отправителя + не было имени ДО момента поиска, а когда транз нашлась, имя появилось
        ElementsCollection matchingTransaction = $(Selectors.byClassName("list-group")).findAll("li");
        matchingTransaction.findBy(Condition.text(TransactionType.TRANSFER_IN.getMessage() + "\n - $" + "\n" + makeDepositResponse.getBalance()));


    }

    @Test
    public void userCanSearchTransactionBySenderUsername() {

    /*
### Тест: юзер-получатель может найти транзакцию трансфер-ин по юзернейму отправителя


Предшаги через апи: админ создает юзера 1 и юзера 2. Юзер 2 имеет имя. Создаем им счета, юзеру 1 делаем депозит 5000. Юзер 1 делает перевод юзеру 2.
Шаг: юзер 2 логинится и жмет кнопку Make a Transfer, а затем Transfer Again. Ввести юзернейм юзера 1.

Ошибка! при поиске находится транзакция трансфер аут -- я вижу транзакции от лица своего отправителя!
     */

        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDepositX2(accountResponse.getId(), user1.getRequest());
        String user1Name = UserSteps.changesNameReturnRequest(user1.getRequest()).getName();
        CreatedUser user2 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        TransferMoneyResponse transferMoneyResponse = UserSteps.transferMoney(accountResponse.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest());

        authAsUserUi(user2.getRequest());
        Selenide.open("/transfer");
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);
        transferAgainButton.click();

        SelenideElement enterNameToFindPlaceholder = $(Selectors.byAttribute("placeholder", "Enter name to find transactions"));
        SelenideElement searchTransactionsButton = $(Selectors.byText("\uD83D\uDD0D Search Transactions"));

        enterNameToFindPlaceholder.sendKeys(user1Name);
        searchTransactionsButton.click();

        //Ошибка:транзакция должна быть трансфер ин, тк мы смотрим у юзера-отправителя
        ElementsCollection matchingTransaction = $(Selectors.byClassName("list-group")).findAll("li");
        matchingTransaction.findBy(Condition.text(TransactionType.TRANSFER_OUT.getMessage() + "\n - $" + "\n" + makeDepositResponse.getBalance()));
    }

}
