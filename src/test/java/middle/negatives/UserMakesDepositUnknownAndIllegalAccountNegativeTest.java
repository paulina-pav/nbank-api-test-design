package middle.negatives;

import Requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import Tests.BaseTest;
import generators.DataGenerator;
import generators.ErrorMessage;
import generators.MaxSumsForDepositAndTransactions;
import generators.UserRole;
import io.restassured.common.mapper.TypeRef;
import models.*;
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



    public static Stream<Arguments> invalidAccounts(){
        return Stream.of(
                Arguments.of(100500, ErrorMessage.UNAUTHORIZED_ACCESS_TO_ACCOUNT.getMessage()) //403 //несуществующий аккаунт
        );
    }

    @ParameterizedTest
    @MethodSource("invalidAccounts")
    //Тест-кейс 1: Авторизованный юзер делает депозит -1 на свой аккаунт на свой аккаунт
    public void userMakesInvalidDeposit(int invalidAccount, String expectedMessage) {

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


        //Шаг 3: сделать депозит
        MakeDeposit deposit = new MakeDeposit
                .Builder()
                .setBalance(MaxSumsForDepositAndTransactions.DEPOSIT.getValue())
                .setId(invalidAccount)
                .build();

        String actualErrorMessage =  new MakeDepositRequester
                (RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()), ResponseSpecs.Forbidden())
                .post(deposit).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedMessage);


        //Убедиться, что пополнения не произошло.
        //проверим что транзакций нет
        List<GetAccountTransactionsResponse> transactionsResponse = new GetAccountTransactionsRequester
                (RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),ResponseSpecs.isOk())
                .get(new GetAccountTransactions(createAnAccResponse.getId()))
                .extract().as(new TypeRef<List<GetAccountTransactionsResponse>>() {});

        List<GetAccountTransactionsResponse> transactions = transactionsResponse.stream().toList();

        soflty.assertThat(transactions).isEmpty();

        //Шаг 5: удалить юзера
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

        //Предусловие шаг 1: создать юзера 1
        NewUserRequest newUser1 = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        //Предусловие шаг2: создать юзер 2. юзер 1 будет пытаться пополнить его счет
        NewUserRequest newUser2 = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();

        NewUserResponse newUserResponse1 = new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser1).extract().as(NewUserResponse.class);

        NewUserResponse newUserResponse2 = new CreateNewUserRequester(RequestSpecs.adminAuth(), ResponseSpecs.entityWasCreated())
                .post(newUser2).extract().as(NewUserResponse.class);

//Предусловие шаг 3: создаем счет юзеру 1 и записываем его в переменную
        CreateAnAccResponse createAnAccResponse1 = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract().as(CreateAnAccResponse.class);
        Integer accountUser1Id = createAnAccResponse1.getId();
        float balanceUser1Id = createAnAccResponse1.getBalance();


//Предусловие шаг 4: создаем счет юзеру 2. Вытащим из него баланс для дальнейших проверок и номер счета
        CreateAnAccResponse createAnAccResponse2 = new CreateAccountRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.entityWasCreated()
        )
                .post(new CreateAnAccount.Builder().build())
                .extract().as(CreateAnAccResponse.class);
        Integer accountUser2Id = createAnAccResponse2.getId();
        float balanceUser2Id = createAnAccResponse2.getBalance();


        //Шаг 5: юзер 1 пытается сделать дебет на счет юзера 2
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
        List<GetCustomerAccountResponse> accountUser1Response = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser1.getUsername(), newUser1.getPassword()),
                ResponseSpecs.isOk())
                .get()
                .extract().as(new TypeRef<List<GetCustomerAccountResponse>>() {});

        List<GetCustomerAccountResponse> accountUser1 = accountUser1Response.stream().toList();

        //Проверим что на его счетах баланс 0
        GetCustomerAccountResponse targetUser1Acc = accountUser1.stream()
                .filter(a->a.getId().equals(accountUser1Id))
                .filter(a->a.getBalance() == 0.0F)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));


        //Проверка 2. Убедимся, что состояние счета юзера 2 не изменилось
        List<GetCustomerAccountResponse> accountsUser2Response = new GetCustomerAccountsRequester(
                RequestSpecs.authAsUser(newUser2.getUsername(), newUser2.getPassword()),
                ResponseSpecs.isOk())
                .get()
                .extract().as(new TypeRef<List<GetCustomerAccountResponse>>() {});
        List<GetCustomerAccountResponse> accountsUser2 = accountsUser2Response.stream().toList();

        //Проверим что на его счетах баланс 0
        GetCustomerAccountResponse targetUser2Acc = accountsUser2.stream()
                .filter(a->a.getId().equals(accountUser2Id))
                .filter(a->a.getBalance() == 0.0F)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Счёт не найден"));


        //Шаг 6: удалим только тех юзеров, что создали в тесте

        //поместим в лист ответы на запросы о создании юзеров
        List<NewUserResponse> createdUsers = new ArrayList<>(List.of(newUserResponse1,newUserResponse2));
        //удалим их. И сразу же вызовем getAllUsers от админа, чтобы проверить, что id не существует
        for(NewUserResponse user: createdUsers){
            String successMessage = new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk())
                    .delete(new DeleteByUserId(user.getId()))
                    .extract().asString();
            String expected = String.format("User with ID %d deleted successfully.", user.getId());
            soflty.assertThat(successMessage).isEqualTo(expected);

            //Убедимся, что удаленного юзера реально нет. Админ вызывает getAllUsers, сразу положим в лист
            List<GetAllUsersResponse> users = new GetAllUsersRequester(
                    RequestSpecs.adminAuth(),
                    ResponseSpecs.isOk()
            ).get().extract().as(new TypeRef<List<GetAllUsersResponse>>() {});

            //вытащим их id в лист
            List<Integer> userIds = users.stream()
                    .map(GetAllUsersResponse::getId)
                    .toList();

            //проверим, что удаленного юзера реально нет
            soflty.assertThat(userIds)
                    .as("удалённый id не должен быть среди пользователей")
                    .doesNotContain(user.getId());
        }

    }

}
