package uisenior.transferagain;

import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.TransactionType;
import api.models.CreateAnAccountResponse;
import api.models.CreatedUser;
import api.models.MakeDepositResponse;
import api.requests.steps.UserSteps;
import common.annotation.Browsers;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uisenior.BaseUiTest;
import ui.pages.TransferAgainPage;
import ui.alerts.AlertsHelpMethods;
import ui.alerts.TransferAgainAlerts;

public class TransferAgainNegativeTest extends BaseUiTest {

    @Test
    @UserSession(value = 2)
    @Browsers({"firefox"})
    public void userCantTransferOutAgainIfAmountMoreThanBalance() {
        /*
### Тест: Юзер не может выбрать трансфер аут транзакцию и отправить ее с той же суммой, что и была, если баланс меньше

Результат: ❌ Transfer failed: Please try again.*/


        CreatedUser user1 = SessionStorage.getUser(1);
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX3(accountResponse1.getId(), user1.getRequest());

        CreatedUser user2 = SessionStorage.getUser(2);
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());

        Double sum = UserSteps.transferMoney(accountResponse1.getId(), accountResponse2.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                user1.getRequest()).getAmount();

        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());


        new TransferAgainPage()
                .open()
                .getTransactionSection()
                .findTransactionByTypeAndSum(TransactionType.TRANSFER_OUT.getMessage(), sum)
                .clickRepeatButton()
                .findConfirmationTextTransferToAccount(accountResponse2.getId())
                .selectYourAccount((accountResponse1.getAccountNumber()))
                .insertAmount(sum)
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
    @UserSession(value = 2)
    @Browsers({"firefox"})
    public void userMustNotInitiateTransferInTransaction() {
        /*

### Тест: Юзер-получатель не может инициировать из трансфер ин транзакции перевод денег себе на счет

Вопрос в том, что перевод якобы на счет дебета, хотя транзакция была трансфер ИН (должна быть на счет юзера 2)!
Ввести сумму не больше баланса счета-кредита и отправить.
Ошибка: перевод самому себе*/


        CreatedUser user1 = SessionStorage.getUser(1); //получатель, им логинимся
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user1.getRequest());


        CreatedUser user2 = SessionStorage.getUser(2); //отправитель
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX4(accountResponse2.getId(), user2.getRequest());

        Double sum = UserSteps.transferMoney(accountResponse2.getId(), accountResponse1.getId(), MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                user2.getRequest()).getAmount();

        //баланс у юзера-дебета сейчас 10000, у юзера-кредита 10 000

        Double user1BalanceBefore = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceBefore = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        new TransferAgainPage()
                .open()
                .getTransactionSection()
                .findTransactionByTypeAndSum(TransactionType.TRANSFER_IN.getMessage(), sum)
                .clickRepeatButton()
                .findConfirmationTextTransferToAccount(accountResponse2.getId())
                .selectYourAccount((accountResponse1.getAccountNumber())) //аккаунт получателя
                .insertAmount(sum)
                .confirm()
                .sendTransfer()
                .checkAlertMessageAndAccept(AlertsHelpMethods.formTransferAgainSuccessfulAlert(sum,
                        accountResponse1.getAccountNumber(), accountResponse1.getAccountNumber()));


        //✅ Transfer of $10000 successful from Account 39 to 39! аккаунты владельца изначальной transfer in


        //проверка балансов: не должны были измениться
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        soflty.assertThat(user1BalanceBefore).isEqualTo(user1BalanceAfter);
        soflty.assertThat(user2BalanceBefore).isEqualTo(user2BalanceAfter);
    }
}
