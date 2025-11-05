package Tests.positive;

import Requests.*;
import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import Tests.BaseTest;
import generators.DataGenerator;
import generators.MaxSumsForDepositAndTransactions;
import generators.TransactionType;
import generators.UserRole;
import models.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class UserMakesDepositTest extends BaseTest {
    //Тест-кейс : Авторизованный юзер делает депозит на свой счет
    @Test
    public void getAccTrans(){
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
                .setId(id)
                .build();

        MakeDepositResponse makeDepositResponse = new MakeDepositRequester
                (RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()), ResponseSpecs.isOk())
                .post(deposit).extract().as(MakeDepositResponse.class);


        //этот запрос -- ключевой в данном тесте. поэтому сделаем много ассертов
        //проверим: номер счета, баланс и что массив с транзакциями не пустой
        soflty.assertThat(makeDepositResponse.getId()).isEqualTo(id);
        soflty.assertThat(makeDepositResponse.getBalance()).isEqualTo(MaxSumsForDepositAndTransactions.DEPOSIT.getValue());
        soflty.assertThat(makeDepositResponse.getTransactions()).isNotEmpty();


        //проверка 2: Затем транзакции: что сумма нужная, тип депозит и связанный счет нужный
        //Для этого ищем нужный объект транзакции в массиве транзакций, подходящую под наши требования: сумма, тип транзакции и счет зачисления
        MakeDepositResponse.Transaction targetTransaction = makeDepositResponse.getTransactions().stream()
                .filter(t -> t.getAmount() == deposit.getBalance()) //баланс такой же как мы положили
                .filter(t -> t.getRelatedAccountId().equals(deposit.getId())) //счет зачисления
                .filter(t -> t.getType().equals(TransactionType.DEPOSIT.getMessage())) //тип транзакции
                .findFirst()
                .orElseThrow(() -> new AssertionError("Такой транзакции не существует"));

        //проверим ее еще раз, но уже на уровне ассертов.
        soflty.assertThat(targetTransaction.getAmount()).isEqualTo(deposit.getBalance());
        soflty.assertThat(targetTransaction.getRelatedAccountId()).isEqualTo(deposit.getId());
        soflty.assertThat(targetTransaction.getType()).isEqualTo(TransactionType.DEPOSIT.getMessage());



        //запрашиваем транзакции по конкретному счету, чтобы еще раз проверить, что депозит действительно совершен на нужный счет и на нужную сумму
        List<GetAccountTransactionsResponse> transactions = new GetAccountTransactionsRequester
                (RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),ResponseSpecs.isOk())
                .get(new GetAccountTransactions(createAnAccResponse.getId())).extract().jsonPath().getList("", GetAccountTransactionsResponse.class);

        //проверим, что среди транзакций есть та, которая подходит под требования: нужный баланс, нужный тип транзакции и нужный id
        transactions.stream()
                        .filter(t-> t.getAmount() ==MaxSumsForDepositAndTransactions.DEPOSIT.getValue())
                                .filter(t->t.getType().equals(TransactionType.DEPOSIT.getMessage()))
                                        .filter(t-> t.getRelatedAccountId() == id)
                                                .findAny()
                .orElseThrow(() -> new AssertionError("Нужная транзакция не найдена"));


        //Удаляем юзеров
        //вернем айди всех и удалим всех
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
