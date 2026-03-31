package apisenior.db.makesdeposit;

import api.generators.ErrorMessage;
import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.TransactionType;
import api.models.CreatedUser;
import api.models.GetCustomerAccountResponse;
import api.models.MakeDepositRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import common.annotation.EnabledForBackend;
import common.backendprofiles.BackendProfile;
import db.models.AccountDao;
import db.models.TransactionDao;
import db.models.comparison.DaoAndModelAssertions;
import db.steps.DBSteps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/*

### Тест-кейс 1: Авторизованный юзер делает депозит -1 на свой аккаунт на свой аккаунт
### Тест-кейс 2: Авторизованный юзер делает депозит 0 на свой аккаунт
### Тест-кейс 3: Авторизованный юзер делает депозит 5001 на свой аккаунт
### Тест-кейс 4: Авторизованный юзер не может сделать депозит на чужой счет

*/

public class UserMakesDepositNegativeTest extends BaseTest {
    public static Stream<Arguments> invalidSum() {
        return Stream.of(
                Arguments.of(-1.0, ErrorMessage.INVALID_ACCOUNT_OR_AMOUNT.getMessage()), //400 //"Invalid account or amount"DEPOSIT_MUST_BE_AT_LEAST_001.getMessage()
                Arguments.of(0.0, ErrorMessage.INVALID_ACCOUNT_OR_AMOUNT.getMessage()),//400 //ErrorMessage.DEPOSIT_MUST_BE_AT_LEAST_001.getMessage()
                Arguments.of(5001.0, ErrorMessage.DEPOSIT_AMOUNT_EXCEEDS_THE_5000_LIMIT.getMessage()) //400ErrorMessage.DEPOSIT_AMOUNT_CANNOT_EXCEED_5000.getMessage()
        );
    }

    @DisplayName("Юзер не может сделать депозит на невалидную сумму")
    @ParameterizedTest
    @MethodSource("invalidSum")
    @EnabledForBackend(BackendProfile.WITH_DATABASE_WITH_FIX)
    public void userMakesInvalidDeposit(Double invalidAmount, String expectedErrorMessage) {

        CreatedUser newUser = createUser();

        Long accountId = UserSteps.createsAccount(newUser.getRequest()).getId();
        Double balanceBefore = UserSteps.getBalance(newUser.getRequest(), accountId);

        MakeDepositRequest deposit = MakeDepositRequest.builder()
                .id(accountId)
                .balance(invalidAmount)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(newUser.getRequest().getUsername(), newUser.getRequest().getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(deposit).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);


        Double balanceAfter = UserSteps.getBalance(newUser.getRequest(), accountId);
        soflty.assertThat(balanceAfter).isEqualTo(balanceBefore);

        //Проверка: на счете нет транзакций Deposit
        boolean isTransaction = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.DEPOSIT.getMax(),
                TransactionType.DEPOSIT.getMessage(), accountId, accountId, newUser.getRequest());
        soflty.assertThat(isTransaction).isFalse();


        GetCustomerAccountResponse currentAccount = UserSteps.getAccount(newUser.getRequest(), accountId);
        AccountDao accountDao = DBSteps.getAccountByUserIdAndBalance(newUser.getResponse().getId(), balanceBefore);
        DaoAndModelAssertions.assertThat(currentAccount, accountDao);


        TransactionDao transactionDao = DBSteps.findTransactionByTypeBySumByAccountIdByRelatedAccountId(TransactionType.TRANSFER_OUT.getMessage(),
                MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), accountId, accountId
        );

        soflty.assertThat(transactionDao).isNull();

    }

    public static Stream<Arguments> invalidAccounts() {
        return Stream.of(
                Arguments.of(ErrorMessage.UNAUTHORIZED_ACCESS_TO_ACCOUNT.getMessage()) //403
        );
    }

    @DisplayName("Юзер не может сделать депозит на чужой счет")
    @ParameterizedTest
    @MethodSource("invalidAccounts")
    @EnabledForBackend(BackendProfile.WITH_DATABASE_WITH_FIX)
    public void userMakesDepositOnOtherUserAccount(String expectedErrorMessage) {

        CreatedUser user1 = createUser();
        CreatedUser user2 = createUser();

        Long user1Acc = UserSteps.createsAccount(user1.getRequest()).getId();
        Long user2Acc = UserSteps.createsAccount(user2.getRequest()).getId();

        Double user1AccBalanceBefore = UserSteps.getBalance(user1.getRequest(), user1Acc);
        Double user2AccBalanceBefore = UserSteps.getBalance(user2.getRequest(), user2Acc);

        MakeDepositRequest deposit = MakeDepositRequest.builder()
                .id(user2Acc) //берем счет юзера 2
                .balance(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .build();

        //юзер 1 пытается сделать депозит на счет юзера 2, авторизуясь как юзер 1
        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(user1.getRequest().getUsername(), user1.getRequest().getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsForbidden()
        ).post(deposit).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);


        //Проверка 1. Убедимся, что балансы на обоих счетах не изменились
        Double user2AccBalanceAfter = UserSteps.getBalance(user2.getRequest(), user2Acc);
        Double user1AccBalanceAfter = UserSteps.getBalance(user1.getRequest(), user1Acc);

        soflty.assertThat(user1AccBalanceAfter.equals(user1AccBalanceBefore));
        soflty.assertThat(user2AccBalanceAfter.equals(user2AccBalanceBefore));


        //Проверка 2. У юзера 1 нет транзакции депозит с упоминанием счета юзера 2
        boolean isDepositInUser1 = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.DEPOSIT.getMax(),
                TransactionType.DEPOSIT.getMessage(), user1Acc, user2Acc, user1.getRequest()
        );
        soflty.assertThat(isDepositInUser1).isFalse();

        boolean isDepositInUser2 = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.DEPOSIT.getMax(),
                TransactionType.DEPOSIT.getMessage(), user2Acc, user2Acc, user2.getRequest()
        );
        soflty.assertThat(isDepositInUser2).isFalse();


        GetCustomerAccountResponse accountsUser1 = UserSteps.getAccount(user1.getRequest(), user1Acc);
        AccountDao accountDaoUser1 = DBSteps.getAccountByUserIdAndBalance(user1.getResponse().getId(), user1AccBalanceBefore);
        DaoAndModelAssertions.assertThat(accountsUser1, accountDaoUser1);


        GetCustomerAccountResponse accountsUser2 = UserSteps.getAccount(user2.getRequest(), user2Acc);
        AccountDao accountDaoUser2 = DBSteps.getAccountByUserIdAndBalance(user2.getResponse().getId(), user2AccBalanceBefore);
        DaoAndModelAssertions.assertThat(accountsUser2, accountDaoUser2);
    }
}
