package apisenior.onlyapi.fraudcheck.positive;

import api.comparison.ModelAssertions;
import api.generators.MaxSumsForDepositAndTransactions;
import api.models.TransferMoneyRequest;
import api.models.TransferMoneyWithFraudCheckResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import common.annotation.FraudCheckMock;
import common.annotation.UsersForApiTests;
import common.extensions.FraudCheckMockExtension;
import common.helper.FraudCheckResponseFactory;
import common.storage.UserForApiStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

public class TransferMoneyWithFraudCheckTest extends BaseTest {


    //1. Юзер успешно переводит деньги на существующий счет другого юзера
//2. Юзер успешно переводит деньги с одного своего счета на другой

    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    @ExtendWith(FraudCheckMockExtension.class)
    @UsersForApiTests(2)
    public void authUserCanMakeTransactionWithFraudCheck(UserForApiStorage context) {

        Long debetId = UserSteps.createAccountAndMakeDepositX2(context.getFirstUser());
        Long creditId = UserSteps.createsAccount(context.getSecondUser().getRequest()).getId();


        Double debetAccBalanceBefore = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        Double creditAccBalanceBefore = UserSteps.getBalance(context.getSecondUser().getRequest(), creditId);

        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .receiverAccountId(creditId)
                .amount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .build();

        TransferMoneyWithFraudCheckResponse response = new ValidatedCrudRequester<TransferMoneyWithFraudCheckResponse>(
                RequestSpecs.authAsUser(context.getFirstUser().getRequest().getUsername(), context.getFirstUser().getRequest().getPassword()),
                Endpoint.TRANSFER_WITH_FRAUD_CHECK,
                ResponseSpecs.requestReturnsOK()
        ).post(transferMoney);

        soflty.assertThat(response).isNotNull();
        ModelAssertions.assertThatModels(response, FraudCheckResponseFactory.getExpectedResult());


        //у счета-дебета уменьшился на сумму перевода, а у счета кредита -- увеличился
        Double debetAccBalanceAfter = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        Double creditAccBalanceAfter = UserSteps.getBalance(context.getSecondUser().getRequest(), creditId);

        soflty.assertThat(debetAccBalanceAfter).isEqualTo(debetAccBalanceBefore - MaxSumsForDepositAndTransactions.TRANSACTION.getMax());
        soflty.assertThat(creditAccBalanceAfter).isEqualTo(creditAccBalanceBefore + MaxSumsForDepositAndTransactions.TRANSACTION.getMax());

    }

    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    @ExtendWith(FraudCheckMockExtension.class)
    @UsersForApiTests
    public void userCanTransferMoneyFromOneAccToAnotherAcc(UserForApiStorage context) {

        Long debetId = UserSteps.createAccountAndMakeDepositX2(context.getFirstUser());
        Long creditId = UserSteps.createsAccount(context.getFirstUser().getRequest()).getId();


        Double debetAccBalanceBefore = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        Double creditAccBalanceBefore = UserSteps.getBalance(context.getFirstUser().getRequest(), creditId);


        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .receiverAccountId(creditId)
                .amount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .build();

        TransferMoneyWithFraudCheckResponse response = new ValidatedCrudRequester<TransferMoneyWithFraudCheckResponse>(
                RequestSpecs.authAsUser(context.getFirstUser().getRequest().getUsername(), context.getFirstUser().getRequest().getPassword()),
                Endpoint.TRANSFER_WITH_FRAUD_CHECK,
                ResponseSpecs.requestReturnsOK()
        ).post(transferMoney);

        soflty.assertThat(response).isNotNull();
        ModelAssertions.assertThatModels(response, FraudCheckResponseFactory.getExpectedResult());

        //у счета-дебета уменьшился на сумму перевода, а у счета кредита -- увеличился
        Double debetAccBalanceAfter = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        Double creditAccBalanceAfter = UserSteps.getBalance(context.getFirstUser().getRequest(), creditId);

        soflty.assertThat(creditAccBalanceAfter).isEqualTo(creditAccBalanceBefore + MaxSumsForDepositAndTransactions.TRANSACTION.getMax());
        soflty.assertThat(debetAccBalanceAfter).isEqualTo(debetAccBalanceBefore - MaxSumsForDepositAndTransactions.TRANSACTION.getMax());
    }
}
