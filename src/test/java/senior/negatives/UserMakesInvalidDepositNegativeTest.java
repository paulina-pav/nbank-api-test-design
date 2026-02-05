package senior.negatives;

import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.TransactionType;
import api.models.MakeDepositRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/*

### Тест-кейс 1: Авторизованный юзер делает депозит -1 на свой аккаунт на свой аккаунт
### Тест-кейс 2: Авторизованный юзер делает депозит 0 на свой аккаунт
### Тест-кейс 3: Авторизованный юзер делает депозит 5001 на свой аккаунт

*/

public class UserMakesInvalidDepositNegativeTest extends senior.BaseTest {
   public static Stream<Arguments> invalidSum() {
        return Stream.of(
                Arguments.of(-1.0, "Deposit amount must be at least 0.01"), //400
                Arguments.of(0.0, "Deposit amount must be at least 0.01"), //400
                Arguments.of(5001.0, "Deposit amount cannot exceed 5000") //400
        );
    }

    @ParameterizedTest
    @MethodSource("invalidSum")
    //Тест-кейс 1: Авторизованный юзер делает депозит -1 на свой аккаунт
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

        //Проверка: баланс не изменился
        Double balanceAfter = UserSteps.getBalance(newUser.getRequest(), accountId);
        soflty.assertThat(balanceAfter).isEqualTo(balanceBefore);

        //Проверка: на счете нет транзакций Deposit
        boolean isTransaction = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.DEPOSIT.getMax(),
                TransactionType.DEPOSIT.getMessage(), accountId, accountId, newUser.getRequest());
        soflty.assertThat(isTransaction).isFalse();
    }
}
