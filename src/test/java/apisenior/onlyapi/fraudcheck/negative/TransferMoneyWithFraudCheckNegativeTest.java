package apisenior.onlyapi.fraudcheck.negative;

import api.generators.ErrorMessage;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class TransferMoneyWithFraudCheckNegativeTest extends BaseTest {
    /*
Кейс 1. Перевод от юзера к юзеру при балансе 5000:
    1. Юзер переводит другому 0
    2. Юзер переводит -1
    3. Юзер переводит 10 001
    4. Юзер переводит 6000

Кейс 2: перевод самому себе со счета на счет при балансе 5000:
-тестовые данные из Кейса 1.

Кейс 3.Юзер переводит другому юзеру сумму 10 001 при балансе больше 10 001
Кейс 4. Юзер переводит сам себе со счета на счет сумму 10 001 при балансе больше 10 001

     */

    public static Stream<Arguments> invalidSumToTransfer() {
        return Stream.of(
                Arguments.of(0.0, ErrorMessage.INSUFFICIENT_FUNDS.getMessage()),
                Arguments.of(-1.0, ErrorMessage.INSUFFICIENT_FUNDS.getMessage()),
                Arguments.of(10001.0, ErrorMessage.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage()),
                Arguments.of(6000.0, ErrorMessage.INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNT.getMessage())
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
    @UsersForApiTests(2)
    @MethodSource("invalidSumToTransfer")
    @ParameterizedTest
    public void userTransferInvalidSumToUser(Double invalidSum, String expectedErrorMessage, UserForApiStorage context) {


        Long debetId = UserSteps.createAccountAndMakeDeposit(context.getFirstUser());
        Long creditId = UserSteps.createsAccount(context.getSecondUser().getRequest()).getId();


        Double debetAccBalanceBefore = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        Double creditAccBalanceBefore = UserSteps.getBalance(context.getSecondUser().getRequest(), creditId);

        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .amount(invalidSum)
                .receiverAccountId(creditId)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(context.getFirstUser().getRequest().getUsername(), context.getFirstUser().getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(transferMoney).extract().asString();

        String newMessage = NormalizerNegativeResponseMessage.normalizeMessage(actualErrorMessage);

        soflty.assertThat(newMessage).isEqualTo(expectedErrorMessage);


        //I. Балансы у обоих не изменились
        Double debetAccBalanceAfter = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        Double creditAccBalanceAfter = UserSteps.getBalance(context.getSecondUser().getRequest(), creditId);

        soflty.assertThat(debetAccBalanceAfter).isEqualTo(debetAccBalanceBefore);
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
    @MethodSource("invalidSumToTransfer")
    @ParameterizedTest
    public void userCanTransferMoneyFromOneAccToAnotherAcc(Double invalidSum, String expectedErrorMessage, UserForApiStorage context) {


        Long debetId = UserSteps.createAccountAndMakeDeposit(context.getFirstUser());
        Long creditId = UserSteps.createsAccount(context.getFirstUser().getRequest()).getId();


        Double debetAccBalanceBefore = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        Double creditAccBalanceBefore = UserSteps.getBalance(context.getFirstUser().getRequest(), creditId);

        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .amount(invalidSum)
                .receiverAccountId(creditId)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(context.getFirstUser().getRequest().getUsername(), context.getFirstUser().getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(transferMoney).extract().asString();

        String newMessage = NormalizerNegativeResponseMessage.normalizeMessage(actualErrorMessage);

        soflty.assertThat(newMessage).isEqualTo(expectedErrorMessage);

        //I. Балансы у обоих не изменились
        Double debetAccBalanceAfter = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        Double creditAccBalanceAfter = UserSteps.getBalance(context.getFirstUser().getRequest(), creditId);

        soflty.assertThat(debetAccBalanceAfter).isEqualTo(debetAccBalanceBefore);
        soflty.assertThat(creditAccBalanceAfter).isEqualTo(creditAccBalanceBefore);
    }



    public static Stream<Arguments> invalidSumToTransferWithBalanceAbove10001() {
        return Stream.of(
                Arguments.of(10001.0, ErrorMessage.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage())
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
    @UsersForApiTests(2)
    @MethodSource("invalidSumToTransferWithBalanceAbove10001")
    @ParameterizedTest
    public void userCantTransferInsufficientSumToUser(Double invalidSum, String expectedErrorMessage, UserForApiStorage context) {


        Long debetId = UserSteps.createAccountAndMakeDepositX3(context.getFirstUser());
        Long creditId = UserSteps.createsAccount(context.getSecondUser().getRequest()).getId();


        Double debetAccBalanceBefore = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        Double creditAccBalanceBefore = UserSteps.getBalance(context.getSecondUser().getRequest(), creditId);

        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .amount(invalidSum)
                .receiverAccountId(creditId)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(context.getFirstUser().getRequest().getUsername(), context.getFirstUser().getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(transferMoney).extract().asString();

        String newMessage = NormalizerNegativeResponseMessage.normalizeMessage(actualErrorMessage);

        soflty.assertThat(newMessage).isEqualTo(expectedErrorMessage);


        //I. Балансы у обоих не изменились
        Double debetAccBalanceAfter = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        Double creditAccBalanceAfter = UserSteps.getBalance(context.getSecondUser().getRequest(), creditId);

        soflty.assertThat(debetAccBalanceAfter).isEqualTo(debetAccBalanceBefore);
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
    @MethodSource("invalidSumToTransferWithBalanceAbove10001")
    @ParameterizedTest
    public void userCantTransferInsufficientSumToThemself(Double invalidSum, String expectedErrorMessage, UserForApiStorage context) {


        Long debetId = UserSteps.createAccountAndMakeDepositX3(context.getFirstUser());
        Long creditId = UserSteps.createsAccount(context.getFirstUser().getRequest()).getId();


        Double debetAccBalanceBefore = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        Double creditAccBalanceBefore = UserSteps.getBalance(context.getFirstUser().getRequest(), creditId);

        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .amount(invalidSum)
                .receiverAccountId(creditId)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(context.getFirstUser().getRequest().getUsername(), context.getFirstUser().getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(transferMoney).extract().asString();

        String newMessage = NormalizerNegativeResponseMessage.normalizeMessage(actualErrorMessage);

        soflty.assertThat(newMessage).isEqualTo(expectedErrorMessage);

        //I. Балансы у обоих не изменились
        Double debetAccBalanceAfter = UserSteps.getBalance(context.getFirstUser().getRequest(), debetId);
        Double creditAccBalanceAfter = UserSteps.getBalance(context.getFirstUser().getRequest(), creditId);

        soflty.assertThat(debetAccBalanceAfter).isEqualTo(debetAccBalanceBefore);
        soflty.assertThat(creditAccBalanceAfter).isEqualTo(creditAccBalanceBefore);
    }
}
