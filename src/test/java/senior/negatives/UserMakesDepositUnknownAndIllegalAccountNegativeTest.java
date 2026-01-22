package senior.negatives;


import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import Tests.BaseTest;
import api.generators.ErrorMessage;
import api.generators.MaxSumsForDepositAndTransactions;
import api.models.NewUserRequest;
import api.models.MakeDepositRequest;
import api.models.GetCustomerAccountResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/*
Тест-кейсы, которые тут есть:
### Тест-кейс 4: Авторизованный юзер делает депозит на несуществующий аккаунт
### Тест-кейс 5: Авторизованный юзер делает депозит на чужой аккаунт
 */

public class UserMakesDepositUnknownAndIllegalAccountNegativeTest extends BaseTest {


    public static Stream<Arguments> invalidAccounts() {
        return Stream.of(
                Arguments.of(100500L, ErrorMessage.UNAUTHORIZED_ACCESS_TO_ACCOUNT.getMessage()) //403 //несуществующий аккаунт
        );
    }

    @ParameterizedTest
    @MethodSource("invalidAccounts")
    //Тест-кейс 1: Авторизованный юзер делает депозит -1 на свой аккаунт на свой аккаунт
    public void userMakesInvalidDeposit(Long invalidAccount, String expectedMessage) {

        //Предусловие, шаг 1: создаем юзера
        NewUserRequest newUser = AdminSteps.createUser();


        //Шаг 3: сделать депозит
        MakeDepositRequest deposit = MakeDepositRequest.builder()
                .id(invalidAccount)
                .balance(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsForbidden()
        ).post(deposit).extract().asString();
        soflty.assertThat(actualErrorMessage).isEqualTo(expectedMessage);


        //Убедиться, что пополнения не произошло.
        //проверим что транзакций нет
        List<GetCustomerAccountResponse> accounts = UserSteps.getsAccounts(newUser);

        soflty.assertThat(accounts).isEmpty();

        //Удалить юзера
        AdminSteps.deletesUser(newUser);
    }

    //### Тест-кейс 5: Авторизованный юзер делает депозит на чужой аккаунт
    public static Stream<Arguments> errorMessage() {
        return Stream.of(
                Arguments.of(ErrorMessage.UNAUTHORIZED_ACCESS_TO_ACCOUNT.getMessage()) //403 //несуществующий аккаунт
        );
    }

    @ParameterizedTest
    @MethodSource("errorMessage")
    public void userMakesDepositOnOtherUserAccount(String expectedErrorMessage) {

        //cоздать 2 юзеров
        List<NewUserRequest> users = new ArrayList<>();
        for (int i = 0; i <= 1; i++) {
            NewUserRequest newUser = AdminSteps.createUser();
            users.add(newUser);
        }
        NewUserRequest user1 = users.get(0);
        NewUserRequest user2 = users.get(1);

        //создадим счета юзерам
        Long user1Id = UserSteps.createsAccount(user1).getId();
        Long user2Id = UserSteps.createsAccount(user2).getId();


        MakeDepositRequest deposit = MakeDepositRequest.builder()
                .id(user1Id) //берем счет юзера 1
                .balance(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .build();

        //юзер 1 пытается сделать депозит на счет юзера 2
        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(user2.getUsername(),user2.getPassword()), //но авторизуемся как юзер 2
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsForbidden()
        ).post(deposit).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);

        //Проверки

        //Проверка 1. Убедимся, что состояние счета юзера 1 не изменилось (хотя на него и не делали дебет)
        //вернем все счета юзера
        List<GetCustomerAccountResponse> accountUser1Response = UserSteps.getsAccounts(user1);

        GetCustomerAccountResponse targetUser1Acc = accountUser1Response.stream()
                .filter(a->a.getId().equals(user1Id))
                .filter(a->a.getBalance() == 0L)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //Проверка 2. Убедимся, что состояние счета юзера 2 не изменилось
        List<GetCustomerAccountResponse> accountUser2Response = UserSteps.getsAccounts(user2);

        GetCustomerAccountResponse targetUser2Acc = accountUser2Response.stream()
                .filter(a->a.getId().equals(user2Id))
                .filter(a->a.getBalance() == 0L)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //Удаляю созданных юзеров
        for (NewUserRequest u : users) {
            AdminSteps.deletesUser(u);
        }
    }
}
