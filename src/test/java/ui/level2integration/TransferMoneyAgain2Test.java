package ui.level2integration;

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
import org.openqa.selenium.Alert;
import ui.BaseUiTest;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TransferMoneyAgain2Test extends BaseUiTest {


    @Test
    public void userCanTransferOutAgain() {
        /*

### Тест: юзер выбирает трансфер аут и отправляет еще раз

Предшаги через апи: админ создает юзера 1 и юзера 2. Создаем им счета, юзеру 1 делаем депозит 5000. Юзер 1 делает перевод юзеру 2.
Шаг: юзер 1 логинится и жмет кнопку Make a Transfer, а затем Transfer Again. Имеется трансфер аут на искомую сумму.
Шаг: Нажать Repeat на транзакцию трансфер аут
Шаг: В новом окне выбрать свой счет, ввести сумму, проверить текст "Confirm transfer to Account ID: (счет получателя - ошибка! Вижу свой счет)" и нажать Send Transfer

Результат:
Ошибка! Запрос уходит самому себе ✅ Transfer of $9 successful from Account 1 to 1!
Появились 2 транзакции: трансфер ин и трансфер аут, когда прислал сам себе!

         */

        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX4(accountResponse1.getId(), user1.getRequest());

        CreatedUser user2 = AdminSteps.createUser();
        String recipientName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());

        TransferMoneyResponse transferMoneyResponse = UserSteps.transferMoney(accountResponse1.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest());

        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());


        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);
        transferAgainButton.click();


        SelenideElement transactionItem =
                $$(".list-group li")
                        .findBy(text(
                                TransactionType.TRANSFER_OUT.getMessage()
                                        + " - $" + transferMoneyResponse.getAmount()
                        ));

        SelenideElement repeatButton = transactionItem.$("button");
        repeatButton.click();

        //модальное окно
        SelenideElement modalHeader = $(Selectors.byText("\uD83D\uDD01 Repeat Transfer")).shouldBe(visible);


        $(".modal-body p")
                .shouldHave(text("Confirm transfer to Account ID:"))
                .shouldHave(text(accountResponse2.getId().toString())).shouldBe(visible);

        SelenideElement selectAccountTest = $(Selectors.byText("Select Your Account:")).shouldBe(visible);

        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse1.getAccountNumber())).shouldBe(visible).click();

        SelenideElement amountText = $(Selectors.byText("Amount:")).shouldBe(visible);
        Integer amount = transferMoneyResponse.getAmount().intValue();

        SelenideElement amountPlaceholder = $(Selectors.byAttribute("value", amount.toString())).shouldBe(visible);

        SelenideElement confirmationText = $(Selectors.byText("Confirm details are correct")).shouldBe(visible);
        SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck")).shouldBe(visible);
        checkbox.click();

        SelenideElement buttonSendTransfer = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        buttonSendTransfer.click();


        //ошибка, перевод происходит сам себе  ✅ Transfer of $10000 successful from Account 16 to 16!

        String[] accNumberArray = accountResponse1.getAccountNumber().split("");
        String accNumber = accNumberArray[3] + accNumberArray[4];

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText)
                .contains("✅ Transfer of $" + amount + " successful from Account " + accNumber + " " + "to " + accNumber);
        alert.accept();


        //проверка балансов: не должны измениться из-за ошибки
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        soflty.assertThat(user1BalanceBefore).isEqualTo(user1BalanceAfter);
        soflty.assertThat(user2BalanceBefore).isEqualTo(user2BalanceAfter);

    }

    @Test
    public void userCantTransferOutAgainIfAmountMoreThanBalance() {
        /*
### Тест: Юзер не может выбрать трансфер аут транзакцию и отправить ее с той же суммой, что и была, если баланс меньше

Предшаги через апи: админ создает юзера 1 и юзера 2. Создаем им счета, юзеру 1 делаем депозит 15000. Юзер 1 делает перевод юзеру 2 на 10000 (осталось 5000)
Шаг: юзер 1 логинится и жмет кнопку Make a Transfer, а затем Transfer Again. Имеются транзакции: депозиты и трансфер аут на 10 000.
Шаг: Нажать Repeat на транзакцию трансфер аут
Шаг: В новом окне выбрать свой счет, сумму больше своего баланса (снова перевести 10 000),
проверить текст "Confirm transfer to Account ID: (счет получателя)" и нажать Send Transfer

Результат: ❌ Transfer failed: Please try again.

         */
        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX3(accountResponse1.getId(), user1.getRequest());

        CreatedUser user2 = AdminSteps.createUser();
        String recipientName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());

        TransferMoneyResponse transferMoneyResponse = UserSteps.transferMoney(accountResponse1.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest());

        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());


        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);
        transferAgainButton.click();


        SelenideElement transactionItem =
                $$(".list-group li")
                        .findBy(text(
                                TransactionType.TRANSFER_OUT.getMessage()
                                //+ " - $" + transferMoneyResponse.getAmount()
                        ));

        SelenideElement repeatButton = transactionItem.$("button");
        repeatButton.click();

        //модальное окно

        SelenideElement modalHeader = $(Selectors.byText("\uD83D\uDD01 Repeat Transfer")).shouldBe(visible);


        $(".modal-body p")
                .shouldHave(text("Confirm transfer to Account ID:"))
                .shouldHave(text(accountResponse2.getId().toString())).shouldBe(visible);

        SelenideElement selectAccountTest = $(Selectors.byText("Select Your Account:")).shouldBe(visible);

        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse1.getAccountNumber())).shouldBe(visible).click();

        SelenideElement amountText = $(Selectors.byText("Amount:")).shouldBe(visible);
        Integer amount = transferMoneyResponse.getAmount().intValue();

        SelenideElement amountPlaceholder = $(Selectors.byAttribute("value", amount.toString())).shouldBe(visible);

        SelenideElement confirmationText = $(Selectors.byText("Confirm details are correct")).shouldBe(visible);
        SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck")).shouldBe(visible);
        checkbox.click();

        SelenideElement buttonSendTransfer = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        buttonSendTransfer.click();


        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText)
                .contains("❌ Transfer failed: Please try again.");
        alert.accept();

        //проверка балансов: не должны измениться
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());


        soflty.assertThat(user1BalanceBefore).isEqualTo(user1BalanceAfter);
        soflty.assertThat(user2BalanceBefore).isEqualTo(user2BalanceAfter);
    }


    @Test
    public void userCanMakeDepositInTransferAgain() {
        /*
### Тест: Повторить депозит через Transfer Again
Предшаги через апи: админ создает юзера 1. Сделать ему депозит 5000.

Шаг: юзер 1 логинится и жмет кнопку Make a Transfer, а затем Transfer Again. Имеется транзакция депозит. Нажать Repeat Transfer. Выбрать счет и сумму.
Результат: ошибка, под капотом запрос /transfer, а не /deposit. + появляются 2 транзакции in/out в списке, баланс не увеличился
         */
        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDeposit(accountResponse1.getId(), user1.getRequest());

        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());

        authAsUserUi(user1.getRequest());
        Selenide.open("/transfer");
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);
        transferAgainButton.click();


        SelenideElement transactionItem =
                $$(".list-group li")
                        .findBy(text(
                                TransactionType.DEPOSIT.getMessage()
                                //+ " - $" + transferMoneyResponse.getAmount()
                        ));

        SelenideElement repeatButton = transactionItem.$("button");
        repeatButton.click();

        //модальное окно

        SelenideElement modalHeader = $(Selectors.byText("\uD83D\uDD01 Repeat Transfer")).shouldBe(visible);


        $(".modal-body p")
                .shouldHave(text("Confirm transfer to Account ID:"))
                .shouldHave(text(accountResponse1.getId().toString())).shouldBe(visible);

        SelenideElement selectAccountTest = $(Selectors.byText("Select Your Account:")).shouldBe(visible);

        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse1.getAccountNumber())).shouldBe(visible).click();

        SelenideElement amountText = $(Selectors.byText("Amount:")).shouldBe(visible);
        Integer amount = depositResponse1.getBalance().intValue();

        SelenideElement amountPlaceholder = $(Selectors.byAttribute("value", amount.toString())).shouldBe(visible);

        SelenideElement confirmationText = $(Selectors.byText("Confirm details are correct")).shouldBe(visible);
        SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck")).shouldBe(visible);
        checkbox.click();

        SelenideElement buttonSendTransfer = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        buttonSendTransfer.click();


        String[] accNumberArray = accountResponse1.getAccountNumber().split("");
        String accNumber = accNumberArray[3] + accNumberArray[4];

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText)
                .contains("✅ Transfer of $" + amount + " successful from Account " + accNumber + " " + "to " + accNumber);
        alert.accept();

        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());


        //ошибка: появляются 2 транзакции трансфер ин и трансфер аут, баланс в итоге не меняется
        soflty.assertThat(user1BalanceBefore).isEqualTo(user1BalanceAfter);

    }


    @Test
    public void userCantRepeatTransferInTransaction() {
        /*

### Тест: Юзер-получатель не может инициировать из трансфер ин транзакции перевод денег себе на счет

Предшаги через апи: админ создает юзера 1 и юзера 2. Создаем им счета, юзеру 1 делаем депозит 5000. Юзер 1 делает перевод юзеру 2.
Шаг: юзер 2 логинится и жмет кнопку Make a Transfer, а затем Transfer Again. Имеется трансфер ин. Нажать Repeat.

Вопрос в том, что перевод якобы на счет дебета, хотя транзакция была трансфер ИН (должна быть на счет юзера 2)!
Ввести сумму не больше баланса счета-кредита и отправить.
Ошибка: перевод самому себе
         */

        CreatedUser user1 = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX4(accountResponse1.getId(), user1.getRequest());

        CreatedUser user2 = AdminSteps.createUser();
        String recipientName = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());

        TransferMoneyResponse transferMoneyResponse = UserSteps.transferMoney(accountResponse1.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), user1.getRequest());

        //баланс у юзера-дебета сейчас 10000, у юзера-кредита 10 000

        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());


        authAsUserUi(user2.getRequest());
        Selenide.open("/transfer");
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again")).shouldBe(visible);
        transferAgainButton.click();


        SelenideElement transactionItem =
                $$(".list-group li")
                        .findBy(text(
                                TransactionType.TRANSFER_IN.getMessage()
                                //+ " - $" + transferMoneyResponse.getAmount()
                        ));

        SelenideElement repeatButton = transactionItem.$("button");
        repeatButton.click();

        //модальное окно

        SelenideElement modalHeader = $(Selectors.byText("\uD83D\uDD01 Repeat Transfer")).shouldBe(visible);


        $(".modal-body p")
                .shouldHave(text("Confirm transfer to Account ID:"))
                .shouldHave(text(accountResponse1.getId().toString())).shouldBe(visible); //тут почему-то аккаунт отправителя

        SelenideElement selectAccountTest = $(Selectors.byText("Select Your Account:")).shouldBe(visible);

        ElementsCollection myAccountsFromDropdown = $(Selectors.byText("-- Choose an account --")).parent().findAll("option");
        myAccountsFromDropdown.findBy(Condition.text(accountResponse2.getAccountNumber())).shouldBe(visible).click();

        SelenideElement amountText = $(Selectors.byText("Amount:")).shouldBe(visible);
        Integer amount = transferMoneyResponse.getAmount().intValue();

        SelenideElement amountPlaceholder = $(Selectors.byAttribute("value", amount.toString())).shouldBe(visible);

        SelenideElement confirmationText = $(Selectors.byText("Confirm details are correct")).shouldBe(visible);
        SelenideElement checkbox = $(Selectors.byAttribute("id", "confirmCheck")).shouldBe(visible);
        checkbox.click();

        SelenideElement buttonSendTransfer = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        buttonSendTransfer.click();


        //✅ Transfer of $10000 successful from Account 39 to 39! аккаунты владельца изначальной transfer in

        String[] accNumberArray = accountResponse2.getAccountNumber().split("");
        String accNumber = accNumberArray[3] + accNumberArray[4];

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        assertThat(alertText)
                .contains("✅ Transfer of $" + amount + " successful from Account " + accNumber + " " + "to " + accNumber);
        alert.accept();

        //проверка балансов: не должны были измениться
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        soflty.assertThat(user1BalanceBefore).isEqualTo(user1BalanceAfter);
        soflty.assertThat(user2BalanceBefore).isEqualTo(user2BalanceAfter);

    }
}
