package ui.transferagain;

import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.TransactionType;
import api.models.CreateAnAccountResponse;
import api.models.MakeDepositResponse;
import api.models.TransferMoneyResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.TransferAgainPage;
import ui.alerts.AlertsHelpMethods;
import ui.alerts.TransferAgainAlerts;

public class TransferAgainNegativeTest extends BaseUiTest {


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
        new TransferAgainPage()
                .open()
                .findAndClickTransaction(TransactionType.TRANSFER_OUT.getMessage(), transferMoneyResponse.getAmount())
                .findConfirmtionTextTransferToAccount(accountResponse2.getId())
                .selectYourAccount((accountResponse1.getAccountNumber()))
                .insertAmount(transferMoneyResponse.getAmount())
                .confirm()
                .sendTransfer()
                .checkAlertMessageAndAccept(TransferAgainAlerts.TRY_AGAIN.getMessage());


        //проверка балансов: не должны измениться
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());


        soflty.assertThat(user1BalanceBefore).isEqualTo(user1BalanceAfter);
        soflty.assertThat(user2BalanceBefore).isEqualTo(user2BalanceAfter);
    }

    @Test
    public void userMustNotRepeatTransferInTransaction() {
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


        String accountNumberWithoutChars= AlertsHelpMethods.getNumbersFromAccountNumber(accountResponse2.getAccountNumber());



        authAsUserUi(user2.getRequest());

        new TransferAgainPage()
                .open()
                .findAndClickTransaction(TransactionType.TRANSFER_IN.getMessage(), transferMoneyResponse.getAmount())
                .findConfirmtionTextTransferToAccount(accountResponse1.getId()) //здесь аккаунт дебета
                .selectYourAccount((accountResponse2.getAccountNumber())) //аккаунт получателя
                .insertAmount(transferMoneyResponse.getAmount())
                .confirm()
                .sendTransfer()
                .checkAlertMessageAndAccept(AlertsHelpMethods.formTransferAgainSuccessfulAlert(transferMoneyResponse.getAmount(),
                        accountResponse1.getAccountNumber(), accountResponse2.getAccountNumber()));


        //✅ Transfer of $10000 successful from Account 39 to 39! аккаунты владельца изначальной transfer in


        //проверка балансов: не должны были измениться
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        soflty.assertThat(user1BalanceBefore).isEqualTo(user1BalanceAfter);
        soflty.assertThat(user2BalanceBefore).isEqualTo(user2BalanceAfter);

    }
}
