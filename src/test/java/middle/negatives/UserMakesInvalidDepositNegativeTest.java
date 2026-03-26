package middle.negatives;


import Requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import Tests.BaseTest;
import generators.DataGenerator;
import generators.UserRole;
import io.restassured.common.mapper.TypeRef;
import models.*;
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
    public static Stream<Arguments> invalidSum(){
        return Stream.of(
                Arguments.of(-1, "Deposit amount must be at least 0.01"), //400
                Arguments.of(0, "Deposit amount must be at least 0.01"), //400
                Arguments.of(5001, "Deposit amount cannot exceed 5000") //400
        );
    }

    @ParameterizedTest
    @MethodSource("invalidSum")
    //Тест-кейс 1: Авторизованный юзер делает депозит -1 на свой аккаунт на свой аккаунт
    public void userMakesInvalidDeposit(int invalidAmount, String expectedErrorMessage){

        //Предусловие шаг 1: создаем юзера
        NewUserRequest newUser = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        NewUserResponse newUserResponse = new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser).extract().as(NewUserResponse.class);

        //Предусловие шаг 2: юзер создает акк. Запишем ответ, чтобы вытащить из него id счета в будущем
        CreateAnAccResponse createAnAccResponse = new CreateAccountRequester(    //в респонзе может вытащить баланс нового счета его id и транзакции (их нет тк счет новый)
                RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(new CreateAnAccount.Builder().build()).extract().as(CreateAnAccResponse.class);

        Integer id = createAnAccResponse.getId();

        //Шаг 3: пополним этот счет на максимальное значение для депозита
        MakeDeposit deposit = new MakeDeposit
                .Builder()
                .setBalance(invalidAmount)
                .setId(id)
                .build();

        String actualErrorMessage =  new MakeDepositRequester
                (RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()), ResponseSpecs.BadRequest())
                .post(deposit).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);

        //Проверка: вернем все счета юзера
        List<GetCustomerAccountResponse> accountsResponse= new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                ResponseSpecs.isOk())
                .get()
                .extract().as(new TypeRef<List<GetCustomerAccountResponse>>() {});
        List<GetCustomerAccountResponse> accounts = accountsResponse.stream().toList();


        //среди счетов найдем тот, куда делали депозит и проверим его баланс
        GetCustomerAccountResponse targetDebAcc = accounts.stream()
                .filter(a -> id.equals(a.getId()))//номер счета
                .filter(a->a.getBalance() == 0.0F)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //проверим, что транзакций на счете нет



        //Шаг 4: удалить юзера
        // удалим только тех, что создали вначале теста
        //здесь создали только 1 юзера, его и удалим по id. Возьмем его из newUserResponse
        String successMessage = new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk())
                .delete(new DeleteByUserId(newUserResponse.getId()))
                .extract().asString();

        String expected = String.format("User with ID %d deleted successfully.", newUserResponse.getId());
        soflty.assertThat(successMessage).isEqualTo(expected);

        //Убедимся, что такого юзера нет. Админ вызывает getAllUsers, сразу положим в лист
        List<GetAllUsersResponse> users = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().as(new TypeRef<List<GetAllUsersResponse>>() {});

        //вытащим их id в лист
        List<Integer> userIds = users.stream()
                .map(GetAllUsersResponse::getId)
                .toList();

        //проверим, что такого id нет
        soflty.assertThat(userIds)
                .as("удалённый id не должен быть среди пользователей")
                .doesNotContain(newUserResponse.getId());
    }

}
