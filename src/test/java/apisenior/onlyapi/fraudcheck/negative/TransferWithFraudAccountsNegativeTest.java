package apisenior.onlyapi.fraudcheck.negative;

import api.generators.ErrorMessage;
import api.generators.MaxSumsForDepositAndTransactions;
import api.models.TransferMoneyRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import common.annotation.FraudCheckMock;
import common.annotation.UsersForApiTests;
import common.extensions.FraudCheckMockExtension;
import common.helper.NormalizerNegativeResponseMessage;
import common.storage.UserForApiStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class TransferWithFraudAccountsNegativeTest extends BaseTest {
    /*
    Кейс 5. Несуществующий счет отправителя
Кейс 6. Несуществующий счет получателя
Кейс 7. При переводе самому себе указать одинаковый счет отправителя и счет получателя
     */

    public static Stream<Arguments> invalidAccToTransfer() {
        return Stream.of(
                Arguments.of(Long.MAX_VALUE, ErrorMessage.INSUFFICIENT_FUNDS.getMessage())
        );
    }

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
    @MethodSource("invalidAccToTransfer")
    @ParameterizedTest
    public void userCantTransferToUndefinedAccount(Long invalidAcc, String expectedErrorMessage, UserForApiStorage context) {

        Long debetId = UserSteps.createAccountAndMakeDepositX2(context.getFirstUser());
        Double debetAccBalanceBefore = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);


        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .amount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .receiverAccountId(invalidAcc)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(context.getFirstUser().getRequest().getUsername(), context.getFirstUser().getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(transferMoney).extract().asString();

        String newMessage = NormalizerNegativeResponseMessage.normalizeMessage(actualErrorMessage);

        soflty.assertThat(newMessage).isEqualTo(expectedErrorMessage);


        //I. Баланс не изменился
        Double debetAccBalanceAfter = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        soflty.assertThat(debetAccBalanceAfter).isEqualTo(debetAccBalanceBefore);

    }

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
    @MethodSource("invalidAccToTransfer")
    @ParameterizedTest
    public void userCantSendMoneyFromNotTheirAccount(Long invalidAcc, String expectedErrorMessage, UserForApiStorage context) {

        Long creditId = UserSteps.createsAccount(context.getSecondUser().getRequest()).getId();
        Double creditAccBalanceBefore = UserSteps.getBalance(context.getSecondUser().getRequest(), creditId);

        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(invalidAcc)
                .amount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .receiverAccountId(creditId)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(context.getFirstUser().getRequest().getUsername(), context.getFirstUser().getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsForbidden()
        ).post(transferMoney).extract().asString();

        String newMessage = NormalizerNegativeResponseMessage.normalizeMessage(actualErrorMessage);

        soflty.assertThat(newMessage).isEqualTo(expectedErrorMessage);


        //I. Баланс не изменился
        Double creditAccBalanceAfter = UserSteps.getBalance(context.getFirstUser().getRequest(), creditId);
        soflty.assertThat(creditAccBalanceAfter).isEqualTo(creditAccBalanceBefore);

    }

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
    @Test
    public void userCantTransferToTheSAmeAccount(UserForApiStorage context) {
//Баг в этом кейсе. Создать одного юзера, сделать ему счет. Использовать его в роли счета отправителя и счета получателя.
//Ожидаемый результат: перевод не происходит. Реальный результат: перевод происходит

        Long debetId = UserSteps.createAccountAndMakeDepositX2(context.getFirstUser());
        Double debetAccBalanceBefore = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);


        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .amount(MaxSumsForDepositAndTransactions.TRANSACTION.getMax())
                .receiverAccountId(debetId)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(context.getFirstUser().getRequest().getUsername(), context.getFirstUser().getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(transferMoney).extract().asString();


        //I. Баланс не изменился
        Double debetAccBalanceAfter = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        soflty.assertThat(debetAccBalanceAfter).isEqualTo(debetAccBalanceBefore);
    }
}
