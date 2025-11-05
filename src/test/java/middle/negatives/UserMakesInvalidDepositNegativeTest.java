package Tests.negatives;


import Requests.*;
import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import Tests.BaseTest;
import generators.DataGenerator;
import generators.UserRole;
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

        //создаем юзера
        NewUserRequest newUser = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new CreateNewUserRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.entityWasCreated())
                .post(newUser);


        //юзер создает акк. Запишем ответ, чтобы вытащить из него id счета в будущем
        CreateAnAccResponse createAnAccResponse = new CreateAccountRequester(    //в респонзе может вытащить баланс нового счета его id и транзакции (их нет тк счет новый)
                RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                ResponseSpecs.entityWasCreated()
        ).post(new CreateAnAccount.Builder().build()).extract().as(CreateAnAccResponse.class);

        Integer id = createAnAccResponse.getId();

        //пополним этот счет на максимальное значение для депозита
        MakeDeposit deposit = new MakeDeposit
                .Builder()
                .setBalance(invalidAmount)
                .setId(id)
                .build();

        String actualErrorMessage =  new MakeDepositRequester
                (RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()), ResponseSpecs.BadRequest())
                .post(deposit).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);

        //вернем все счета юзера
        List<GetCustomerAccountResponse> accounts= new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                ResponseSpecs.isOk())
                .get().extract()
                .jsonPath()
                .getList("", GetCustomerAccountResponse.class);

        //среди счетов найдем тот, куда делали депозит и проверим его баланс (на самом деле счет всего 1)
        GetCustomerAccountResponse targetDebAcc = accounts.stream()
                .filter(a -> id.equals(a.getId()))//номер счета
                .filter(a->a.getBalance() == 0.0F)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //удалим всех
        List<Integer> userIds = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().jsonPath().getList("id",  Integer.class);

        for (Integer userId : userIds){
            new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk()).delete(new DeleteByUserId(userId));

        }
    }

}
