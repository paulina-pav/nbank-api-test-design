package senior.negatives;

import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import Tests.BaseTest;
import api.models.NewUserRequest;
import api.models.MakeDepositRequest;
import api.models.GetCustomerAccountResponse;

import api.models.GetAccountTransactionsResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

/*

### Тест-кейс 1: Авторизованный юзер делает депозит -1 на свой аккаунт на свой аккаунт
### Тест-кейс 2: Авторизованный юзер делает депозит 0 на свой аккаунт
### Тест-кейс 3: Авторизованный юзер делает депозит 5001 на свой аккаунт

*/

public class UserMakesInvalidDepositNegativeTest extends BaseTest {
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

        //Предусловие шаг 1: создаем юзера
        NewUserRequest user = AdminSteps.createUser();

        //Создать счет
        Long userAccount = UserSteps.createsAccount(user).getId();

        //Сделать депозит
        MakeDepositRequest deposit = MakeDepositRequest.builder()
                .id(userAccount)
                .balance(invalidAmount)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(deposit).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);

        //Проверка: запросим счета юзера, найдем созданный счет и посмотрим, что на него ничего не попало
        List<GetCustomerAccountResponse> accountUserResponse = UserSteps.getsAccounts(user);

        GetCustomerAccountResponse targetUserAcc = accountUserResponse.stream()
                .filter(a -> a.getId().equals(userAccount))
                .filter(a -> a.getBalance() == 0L)
                .findAny()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //Проверка: на счете нет транзакций Deposit
        List<GetAccountTransactionsResponse> transactions = UserSteps.getsAccountTransaction(user, userAccount);
        soflty.assertThat(transactions).isEmpty();

        AdminSteps.deletesUser(user);
    }
}
