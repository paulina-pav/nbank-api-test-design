package Tests.negatives;

import Requests.*;
import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import Tests.BaseTest;
import generators.DataGenerator;
import generators.ErrorMessage;
import generators.MaxSumsForDepositAndTransactions;
import generators.UserRole;
import models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;
    /*
Тест-кейсы, которые тут есть:

### Тест-кейс 4: Авторизованный юзер делает депозит на несуществующий аккаунт
### Тест-кейс 5: Авторизованный юзер делает депозит на чужой аккаунт

 */

public class UserMakesDepositUnknownAndIllegalAccountNegativeTest extends BaseTest {



    public static Stream<Arguments> invalidAccounts(){
        return Stream.of(
                Arguments.of(100500, ErrorMessage.UNAUTHORIZED_ACCESS_TO_ACCOUNT.getMessage()) //403 //несуществующий аккаунт
        );
    }

    @ParameterizedTest
    @MethodSource("invalidAccounts")
    //Тест-кейс 1: Авторизованный юзер делает депозит -1 на свой аккаунт на свой аккаунт
    public void userMakesInvalidDeposit(int invalidAccount, String expectedMessage) {


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
                .setBalance(MaxSumsForDepositAndTransactions.DEPOSIT.getValue())
                .setId(invalidAccount)
                .build();

        String actualErrorMessage =  new MakeDepositRequester
                (RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()), ResponseSpecs.Forbidden())
                .post(deposit).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedMessage);


        //удалим всех
        List<Integer> userIds = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().jsonPath().getList("id",  Integer.class);

        for (Integer userId : userIds){
            new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk()).delete(new DeleteByUserId(userId));

        }
    }


    //### Тест-кейс 5: Авторизованный юзер делает депозит на чужой аккаунт
    public static Stream<Arguments> errorMessage(){
        return Stream.of(
                Arguments.of(ErrorMessage.UNAUTHORIZED_ACCESS_TO_ACCOUNT.getMessage()) //403 //несуществующий аккаунт
        );
    }

    @ParameterizedTest
    @MethodSource("errorMessage")
    public void userMakesDepositOnOtherUserAccount(String expectedErrorMessage ){
        /*
        создать 2 юзера
        Создать каждому по счету
        сделать так, чтобы юзер 1 попытался сделать депозит на юзер 2
         */

        //юзер 1.
        NewUserRequest newUser1 = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        //юзер 2. юзер 1 будет пытаться пополнить его счет
        NewUserRequest newUser2 = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser1);

        new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser2);

//создаем счет юзеру 1 и записываем его в переменную
        CreateAnAccResponse createAnAccResponse1 = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract().as(CreateAnAccResponse.class);
        Integer accountUser1Id = createAnAccResponse1.getId();
        float balanceUser1Id = createAnAccResponse1.getBalance();


//создаем счет юзеру 2. Вытащим из него баланс для дальнейших проверок и номер счета
        CreateAnAccResponse createAnAccResponse2 = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract().as(CreateAnAccResponse.class);
        Integer accountUser2Id = createAnAccResponse2.getId();
        float balanceUser2Id = createAnAccResponse2.getBalance();


        //юзер 1 пытается сделать дебет на счет юзера 2
        String actualErrorMessage = new MakeDepositRequester
                (RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()), ResponseSpecs.Forbidden())
                .post(new MakeDeposit
                        .Builder()
                        .setBalance(MaxSumsForDepositAndTransactions.DEPOSIT.getValue())
                        .setId(accountUser2Id)
                        .build()
                ).extract().asString();
        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage);

        //Проверки

        //Проверка 1. Убедимся, что состояние счета юзера 1 не изменилось (хотя на него и не делали дебет)
        //вернем все счета юзера
        List<GetCustomerAccountResponse> accountUser1= new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.isOk())
                .get().extract()
                .jsonPath()
                .getList("", GetCustomerAccountResponse.class);

        //Проверим что на его счетах баланс 0
        GetCustomerAccountResponse targetUser1Acc = accountUser1.stream()
                .filter(a->a.getId().equals(accountUser1Id))
                .filter(a->a.getBalance() == 0.0F)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));


        //Проверка 2. Убедимся, что состояние счета юзера 2 не изменилось
        List<GetCustomerAccountResponse> accountsUser2= new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.isOk())
                .get().extract()
                .jsonPath()
                .getList("", GetCustomerAccountResponse.class);

        //Проверим что на его счетах баланс 0
        GetCustomerAccountResponse targetUser2Acc = accountsUser2.stream()
                .filter(a->a.getId().equals(accountUser2Id))
                .filter(a->a.getBalance() == 0.0F)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));

        //выпишем айди всех юзеров
        List<Integer> ids = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().jsonPath().getList("id",  Integer.class);

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
