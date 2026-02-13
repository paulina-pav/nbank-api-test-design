package ui.transfermoney;

import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.RandomModelGenerator;
import api.models.CreateAnAccountResponse;
import api.models.MakeDepositResponse;
import api.models.UserChangeNameRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.TransferMoneyPage;
import ui.alerts.AlertsHelpMethods;
import ui.alerts.TransferAlerts;

public class TransferMoneyNegativeTest extends BaseUiTest {
    @Test
    public void userCantTransferWithoutSenderAcc() {
/*
##Тест: юзер не может отправить трансфер без счета отправителя

Валидация на уровне UI, запрос не уходит
*/

        CreatedUser user1 = createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), user1.getRequest());


        CreatedUser user2 = createUser();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        String user2Name = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();

        authAsUserUi(user1.getRequest());

        new TransferMoneyPage()
                .open()
                .enterRecipientName(user2Name)
                .enterRecipientAccount(accountResponse2.getAccountNumber())
                .enterAmount(makeDepositResponse.getBalance())
                .selectEmptyConfirmationCheckbox()
                .clickTransferMoneyButton()
                .checkAlertMessageAndAccept(TransferAlerts.FILL_ALL_FIELDS.getMessage());
    }
    @Test
    public void userCantTransferMoneyWithoutRecipientAccNumberAndName() {

/*
### Тест: юзер не может отправить трансфер без счета получателя и его имени
Результат: ❌ Please fill all fields and confirm. (ошибка от фронта, запрос не уходит)
 */

        CreatedUser user = createUser();

        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user.getRequest());

        authAsUserUi(user.getRequest());


        //select your acc
        new TransferMoneyPage()
                .open()
                .selectSenderAccount(accountResponse.getAccountNumber())
                .enterAmount(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .selectEmptyConfirmationCheckbox()
                .clickTransferMoneyButton()
                .checkAlertMessageAndAccept(TransferAlerts.FILL_ALL_FIELDS.getMessage());
    }

    @Test
    public void userCantTransferMoneyWithoutSum() {
        /*
        ### Тест: юзер не может отправить перевод без суммы
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

        new TransferMoneyPage()
                .open()
                .selectSenderAccount(accountResponse.getAccountNumber())
                .enterRecipientName(user2Name)
                .enterRecipientAccount(accountResponse2.getAccountNumber())
                .selectEmptyConfirmationCheckbox()
                .clickTransferMoneyButton()
                .checkAlertMessageAndAccept(TransferAlerts.FILL_ALL_FIELDS.getMessage());
    }
    @Test
    public void userCantTransferWithoutCheckbox() {
        /*
        ### Тест: юзер не может сделать трансфер без чекбокса
Результат: ❌ Please fill all fields and confirm. Проверка на уровне UI
         */

        CreatedUser user1 = createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), user1.getRequest());


        CreatedUser user2 = createUser();
        CreateAnAccountResponse accountResponse2 = UserSteps.createsAccount(user2.getRequest());
        String user2Name = UserSteps.changesNameReturnRequest(user2.getRequest()).getName();

        authAsUserUi(user1.getRequest());

        new TransferMoneyPage()
                .open()
                .selectSenderAccount(accountResponse.getAccountNumber())
                .enterRecipientName(user2Name)
                .enterRecipientAccount(accountResponse2.getAccountNumber())
                .enterAmount(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .clickTransferMoneyButton()
                .checkAlertMessageAndAccept(TransferAlerts.FILL_ALL_FIELDS.getMessage());

    }

    @Test
    public void userCantMakeTransferToItsAccId() {
        /*
        ### Тест: юзер не может сделать трансфер на свой id счета
Результат: ❌ You cannot transfer money to the same account. Запрос не ушел
         */

        CreatedUser user1 = createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), user1.getRequest());

        authAsUserUi(user1.getRequest());


        new TransferMoneyPage()
                .open()
                .selectSenderAccount(accountResponse.getAccountNumber())
                .enterRecipientAccount(accountResponse.getId().toString())
                .enterAmount(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .selectEmptyConfirmationCheckbox()
                .clickTransferMoneyButton()
                .checkAlertMessageAndAccept(TransferAlerts.TRANSFER_TO_THE_SAME_ACCOUNT.getMessage());
    }

    @Test
    public void userCantTransferMoneyByAccId() {
        /*
### Тест: юзер отправляет деньги на существующий id счета

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

        new TransferMoneyPage()
                .open()
                .selectSenderAccount(accountResponse1.getAccountNumber())
                .enterRecipientName(recipientName)
                .enterRecipientAccount(accountResponse2.getId().toString())
                .enterAmount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .selectEmptyConfirmationCheckbox()
                .clickTransferMoneyButton()
                .checkAlertMessageAndAccept(TransferAlerts.NO_USER_WITH_THIS_ACCOUNT_NUMBER.getMessage());

        //получим балансы после
        Double user1BalanceAfter = UserSteps.getBalance(user1.getRequest(), accountResponse1.getId());
        Double user2BalanceAfter = UserSteps.getBalance(user2.getRequest(), accountResponse2.getId());

        //балансы не должны были поменяться
        soflty.assertThat(user1BalanceAfter).isEqualTo(user1BalanceBefore);
        soflty.assertThat(user2BalanceAfter).isEqualTo(user2BalanceBefore);
    }

    @Test
    public void userCantTransferMoneyToUnknownAcc() {
        /*

### Тест: юзер отправляет деньги на свой id счета
Результат: ❌ No user found with this account number. Запрос не ушел
         */

        CreatedUser user1 = createUser();
        CreateAnAccountResponse accountResponse = UserSteps.createsAccount(user1.getRequest());
        MakeDepositResponse makeDepositResponse = UserSteps.makesDeposit(accountResponse.getId(), user1.getRequest());

        String unknownAcc = RandomModelGenerator.generate(UserChangeNameRequest.class).getName(); //сделала имя произвольной строкой

        authAsUserUi(user1.getRequest());

        new TransferMoneyPage()
                .open()
                .selectSenderAccount(accountResponse.getAccountNumber())
                .enterRecipientAccount(unknownAcc)
                .enterAmount(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .selectEmptyConfirmationCheckbox()
                .clickTransferMoneyButton()
                .checkAlertMessageAndAccept(TransferAlerts.NO_USER_WITH_THIS_ACCOUNT_NUMBER.getMessage());
    }
    @Test
    public void userCantTransferMoneyMoreThanHas() {
       /*
### Тест: юзер не может отправить денег больше, чем есть на балансе
❌ Error: Invalid transfer: insufficient funds or invalid accounts
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

        new TransferMoneyPage()
                .open()
                .selectSenderAccount(accountResponse1.getAccountNumber())
                .enterRecipientName(recipientName)
                .enterRecipientAccount(accountResponse2.getAccountNumber())
                .enterAmount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .selectEmptyConfirmationCheckbox()
                .clickTransferMoneyButton()
                .checkAlertMessageAndAccept(TransferAlerts.INSUFFICIENT_FUNDS.getMessage());


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

Результат:❌ The recipient name does not match the registered name.
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

        new TransferMoneyPage()
                .open()
                .selectSenderAccount(accountResponse1.getAccountNumber())
                .enterRecipientAccount(accountResponse2.getAccountNumber())
                .enterAmount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .selectEmptyConfirmationCheckbox()
                .clickTransferMoneyButton()
                .checkAlertMessageAndAccept(TransferAlerts.RECIPIENT_NAME_NOT_MATCH.getMessage());

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
Ошибка ✅ Successfully transferred $2 to account ACC5!, ошибка бэка!

         */

        CreatedUser user = AdminSteps.createUser();
        CreateAnAccountResponse accountResponse1 = UserSteps.createsAccount(user.getRequest());
        MakeDepositResponse depositResponse1 = UserSteps.makesDepositX2(accountResponse1.getId(), user.getRequest());
        Double userBalanceBefore = UserSteps.getBalance(user.getRequest(), accountResponse1.getId());

        authAsUserUi(user.getRequest());

        new TransferMoneyPage()
                .open()
                .selectSenderAccount(accountResponse1.getAccountNumber())
                .enterRecipientAccount(accountResponse1.getAccountNumber())
                .enterAmount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .selectEmptyConfirmationCheckbox()
                .clickTransferMoneyButton()
                .checkAlertMessageAndAccept(AlertsHelpMethods.formTransferSuccessfulAlert(MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                        accountResponse1.getAccountNumber()));

        //ошибка, отправили сами себе
        //получим баланс после
        Double userBalanceAfter = UserSteps.getBalance(user.getRequest(), accountResponse1.getId());
        //баланс не изменился
        soflty.assertThat(userBalanceAfter).isEqualTo(userBalanceBefore);

    }


}
