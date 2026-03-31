package apisenior.onlyapi.transfermoney;


import api.generators.ErrorMessage;
import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.TransactionType;
import api.models.TransferMoneyRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.UserSteps;
import api.models.CreatedUser;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import common.annotation.EnabledForBackend;
import common.backendprofiles.BackendProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class UserTransferNegativeTest extends BaseTest {
 /*

Тест-кейсы этого файла:

1. Юзер переводит 0 (при балансе 5000)
2. Юзер переводит -1 (при балансе 5000)
3. Юзер переводит 10 001 (при балансе 5000)
4. Юзер переводит 6000 (при балансе в 5000)
5. Юзер переводит сумму 10 001 при балансе больше 10 001

*/


    public static Stream<Arguments> invalidSumToTransfer() {
        return Stream.of(
                Arguments.of(0.0, ErrorMessage.TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001.getMessage()),
                Arguments.of(-1.0, ErrorMessage.TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001.getMessage()),
                Arguments.of(10001.0, ErrorMessage.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage()),
                Arguments.of(6000.0, ErrorMessage.INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNT.getMessage())
        );
    }

    @DisplayName("Юзер не может переводить невалидную сумму при балансе 5000")
    @ParameterizedTest
    @MethodSource("invalidSumToTransfer")
    @EnabledForBackend(BackendProfile.WITH_VALIDATION_FIX)
    public void userTransferInvalidSumToUser(Double invalidSum, String expectedErrorMessage) {

        CreatedUser userDeb = createUser();
        CreatedUser userCred = createUser();

        Long debetId = UserSteps.createsAccount(userDeb.getRequest()).getId();
        Long creditId = UserSteps.createsAccount(userCred.getRequest()).getId();

        UserSteps.makesDeposit(debetId, userDeb.getRequest());

        Double balanceDebetBeforeTransfer = UserSteps.getBalance(userDeb.getRequest(), debetId);
        Double balanceCreditBeforeTransfer = UserSteps.getBalance(userCred.getRequest(), creditId);

        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .amount(invalidSum)
                .receiverAccountId(creditId)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(userDeb.getRequest().getUsername(), userDeb.getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(transferMoney).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);


        //I. Балансы у обоих не изменились
        Double debetAccBalanceAfter = UserSteps.getBalance(userDeb.getRequest(), debetId);
        Double creditAccBalanceAfter = UserSteps.getBalance(userCred.getRequest(), creditId);

        soflty.assertThat(debetAccBalanceAfter).isEqualTo(balanceDebetBeforeTransfer);
        soflty.assertThat(creditAccBalanceAfter).isEqualTo(balanceCreditBeforeTransfer);


        //II. Проверка, что у каждого из счетов не было соответствующей
        boolean isTransactionTransferOut = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                TransactionType.TRANSFER_OUT.getMessage(), debetId, creditId, userDeb.getRequest());
        soflty.assertThat(isTransactionTransferOut).isFalse();


        boolean isTransactionTransferIn = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                TransactionType.TRANSFER_IN.getMessage(), creditId, debetId, userCred.getRequest());
        soflty.assertThat(isTransactionTransferIn).isFalse();
    }


    // 5. Юзер переводит сумму 10 001 при балансе больше 10 001
    public static Stream<Arguments> insufficientSumToTransfer() {
        return Stream.of(
                Arguments.of(10001.0, ErrorMessage.TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage())
        );
    }

    @DisplayName("Юзер не может переводить сумму больше 10 000 при балансе больше 10 000")
    @ParameterizedTest
    @MethodSource("insufficientSumToTransfer")
    @EnabledForBackend(BackendProfile.WITH_VALIDATION_FIX)
    public void userTransferInsufficientSumToUser(Double invalidSum, String expectedErrorMessage) {

        CreatedUser userDeb = createUser();
        CreatedUser userCred = createUser();

        Long debetId = UserSteps.createsAccount(userDeb.getRequest()).getId();
        Long creditId = UserSteps.createsAccount(userCred.getRequest()).getId();

        UserSteps.makesDepositX3(debetId, userDeb.getRequest());

        Double balanceDebetBeforeTransfer = UserSteps.getBalance(userDeb.getRequest(), debetId);
        Double balanceCreditBeforeTransfer = UserSteps.getBalance(userCred.getRequest(), creditId);

        TransferMoneyRequest transferMoney = TransferMoneyRequest.builder()
                .senderAccountId(debetId)
                .amount(invalidSum)
                .receiverAccountId(creditId)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(userDeb.getRequest().getUsername(), userDeb.getRequest().getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(transferMoney).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);


        Double debetAccBalanceAfter = UserSteps.getBalance(userDeb.getRequest(), debetId);
        Double creditAccBalanceAfter = UserSteps.getBalance(userCred.getRequest(), creditId);

        soflty.assertThat(debetAccBalanceAfter).isEqualTo(balanceDebetBeforeTransfer);
        soflty.assertThat(creditAccBalanceAfter).isEqualTo(balanceCreditBeforeTransfer);

        boolean isTransactionTransferOut = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                TransactionType.TRANSFER_OUT.getMessage(), debetId, creditId, userDeb.getRequest());
        soflty.assertThat(isTransactionTransferOut).isFalse();


        boolean isTransactionTransferIn = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.TRANSACTION.getMax(),
                TransactionType.TRANSFER_IN.getMessage(), creditId, debetId, userCred.getRequest());
        soflty.assertThat(isTransactionTransferIn).isFalse();

    }
}

