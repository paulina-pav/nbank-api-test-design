package apisenior.onlyapi.fraudcheck.positive;

import api.generators.GetTransactionWithFraudStatus;
import api.models.CheckFraudDetectionStatusResponse;
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
import common.storage.UserForApiStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

public class GetFraudCheckStatusTest extends BaseTest {
    @Test
    @UsersForApiTests(2)
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    @ExtendWith(FraudCheckMockExtension.class)
    public void authUserTransferredMoneyToAnotherUserAndCheckTransactionFraudStatus(UserForApiStorage context){
        TransferMoneyWithFraudCheckResponse transfer = UserSteps.transferMoneyWithFraudCheck(context.getFirstUser(), context.getSecondUser());

        CheckFraudDetectionStatusResponse checkResponse = new ValidatedCrudRequester<CheckFraudDetectionStatusResponse>(
                RequestSpecs.authAsUser(context.getFirstUser().getRequest().getUsername(), context.getFirstUser().getRequest().getPassword()),
                Endpoint.CHECK_FRAUD_DETECTION_STATUS,
                ResponseSpecs.requestReturnsOK()
        ).get(transfer.getTransactionId());

        soflty.assertThat(checkResponse.getTransactionId()).isEqualTo(transfer.getTransactionId());
        soflty.assertThat(checkResponse.getStatus()).isEqualTo(GetTransactionWithFraudStatus.STATUS_NO_FRAUD_CHECK_REQUIRED);
        soflty.assertThat(checkResponse.getNote()).isEqualTo(GetTransactionWithFraudStatus.NOTE_DOES_NOT_REQUIRE);
    }

    @Test
    @UsersForApiTests(2)
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    @ExtendWith(FraudCheckMockExtension.class)
    public void authUserTransferredMoneyToThemselfAndCheckTransactionFraudStatus(UserForApiStorage context){
        TransferMoneyWithFraudCheckResponse transfer = UserSteps.userTransferMoneyToThemself(context.getFirstUser());

        CheckFraudDetectionStatusResponse checkResponse = new ValidatedCrudRequester<CheckFraudDetectionStatusResponse>(
                RequestSpecs.authAsUser(context.getFirstUser().getRequest().getUsername(), context.getFirstUser().getRequest().getPassword()),
                Endpoint.CHECK_FRAUD_DETECTION_STATUS,
                ResponseSpecs.requestReturnsOK()
        ).get(transfer.getTransactionId());

        soflty.assertThat(checkResponse.getTransactionId()).isEqualTo(transfer.getTransactionId());
        soflty.assertThat(checkResponse.getStatus()).isEqualTo(GetTransactionWithFraudStatus.STATUS_NO_FRAUD_CHECK_REQUIRED);
        soflty.assertThat(checkResponse.getNote()).isEqualTo(GetTransactionWithFraudStatus.NOTE_DOES_NOT_REQUIRE);
    }
}
