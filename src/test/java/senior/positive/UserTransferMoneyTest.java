package senior.positive;


import api.comparison.ModelAssertions;
import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.ServiceMessages;
import api.generators.TransactionType;
import api.models.CreateAnAccountResponse;
import api.models.TransferMoneyRequest;
import api.models.TransferMoneyResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.junit.jupiter.api.Test;

import java.util.List;


public class UserTransferMoneyTest extends senior.BaseTest {

//Тест-кейсы этого файла:
//1. Юзер успешно переводит деньги на существующий счет другого юзера
//2. Юзер успешно переводит деньги с одного своего счета на другой

    @Test
    public void userTransferMoneyFromOneAccToAnotherAcc() {
        CreatedUser newUser = createUser();

        List<CreateAnAccountResponse> newAccounts = UserSteps.createTwoAccounts(newUser.getRequest());

        Long debetId = newAccounts.get(0).getId();
        Long creditId = newAccounts.get(1).getId();

        UserSteps.makesDepositX2(debetId, newUser.getRequest());

        Double debetAccBalanceBefore = UserSteps.getBalance(newUser.getRequest(), debetId);
        Double creditAccBalanceBefore = UserSteps.getBalance(newUser.getRequest(), creditId);

        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .amount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .receiverAccountId(creditId)
                .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(newUser.getRequest().getUsername(), newUser.getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK()
        ).post(transferMoney);
        ModelAssertions.assertThatModels(transferMoney, transferMoneyResponse).match();
        soflty.assertThat(transferMoneyResponse.getMessage()).isEqualTo(ServiceMessages.SUCCESSFUL_TRANSFER.getMessage());


        //у счета-дебета уменьшился на сумму перевода, а у счета кредита -- увеличился
        Double debetAccBalanceAfter = UserSteps.getBalance(newUser.getRequest(), debetId);
        Double creditAccBalanceAfter = UserSteps.getBalance(newUser.getRequest(), creditId);

        soflty.assertThat(debetAccBalanceAfter).isEqualTo(debetAccBalanceBefore - MaxSumsForDepositAndTransactions.TRANSACTION.getMax());
        soflty.assertThat(creditAccBalanceAfter).isEqualTo(creditAccBalanceBefore + MaxSumsForDepositAndTransactions.TRANSACTION.getMax());


        //II. Проверка, что у каждого из счетов была соответствующая транзакция на соответствующую сумму
        boolean isTransactionTransferOut = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                TransactionType.TRANSFER_OUT.getMessage(), debetId, creditId, newUser.getRequest());


        soflty.assertThat(isTransactionTransferOut).isTrue();


        boolean isTransactionTransferIn = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                TransactionType.TRANSFER_IN.getMessage(), creditId, debetId, newUser.getRequest());
        soflty.assertThat(isTransactionTransferIn).isTrue();
    }

    @Test
    // Юзер успешно переводит деньги на существующий счет другого юзера
    public void userTransfersMoneyToUser() {

        CreatedUser userDeb = createUser();
        CreatedUser userCred = createUser();

        Long debetId = UserSteps.createsAccount(userDeb.getRequest()).getId();
        Long creditId = UserSteps.createsAccount(userCred.getRequest()).getId();

        UserSteps.makesDepositX2(debetId, userDeb.getRequest());

        Double balanceDebetBeforeTransfer = UserSteps.getBalance(userDeb.getRequest(), debetId);
        Double balanceCreditBeforeTransfer = UserSteps.getBalance(userCred.getRequest(), creditId);



        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .amount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .receiverAccountId(creditId)
                .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(userDeb.getRequest().getUsername(), userDeb.getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK()
        ).post(transferMoney);
        ModelAssertions.assertThatModels(transferMoney, transferMoneyResponse).match();
        soflty.assertThat(transferMoneyResponse.getMessage()).isEqualTo(ServiceMessages.SUCCESSFUL_TRANSFER.getMessage());

        //I. Баланс у счета дебета уменьшился на искомую сумму, а у кредита -- увеличится на столько же; и текущие балансы в целом верны
        Double debetAccBalanceAfter = UserSteps.getBalance(userDeb.getRequest(), debetId);
        Double creditAccBalanceAfter = UserSteps.getBalance(userCred.getRequest(), creditId);

        soflty.assertThat(debetAccBalanceAfter).isEqualTo(balanceDebetBeforeTransfer - MaxSumsForDepositAndTransactions.TRANSACTION.getMax());
        soflty.assertThat(creditAccBalanceAfter).isEqualTo(balanceCreditBeforeTransfer + MaxSumsForDepositAndTransactions.TRANSACTION.getMax());


        //II. Проверка, что у каждого из счетов была соответствующая транзакция на соответствующую сумму
        boolean isTransactionTransferOut = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                TransactionType.TRANSFER_OUT.getMessage(), debetId, creditId, userDeb.getRequest());
        soflty.assertThat(isTransactionTransferOut).isTrue();


        boolean isTransactionTransferIn = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                TransactionType.TRANSFER_IN.getMessage(), creditId, debetId, userCred.getRequest());
        soflty.assertThat(isTransactionTransferIn).isTrue();
    }

}