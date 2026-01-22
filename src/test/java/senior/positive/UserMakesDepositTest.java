package senior.positive;


import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import Tests.BaseTest;
import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.TransactionType;
import api.models.NewUserRequest;
import api.models.CreateAnAccountResponse;
import api.models.MakeDepositRequest;
import api.models.MakeDepositResponse;
import api.models.Transaction;
import api.models.GetAccountTransactionsResponse;
import org.junit.jupiter.api.Test;
import java.util.List;


public class UserMakesDepositTest extends BaseTest {

    //Тест-кейс : Авторизованный юзер делает депозит на свой счет

    @Test
    public void authUserMakesDeposit() {

        //Создаем юзера
        NewUserRequest newUser = AdminSteps.createUser();

        //Юзер создает акк.
        CreateAnAccountResponse createAnAccResponse = UserSteps.createsAccount(newUser);
        Long accountId = UserSteps.createsAccount(newUser).getId();

        //Юзер делает депозит. Пополним этот счет на максимальное значение для депозита
        MakeDepositRequest deposit = MakeDepositRequest.builder()
                .id(accountId)
                .balance(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .build();

        MakeDepositResponse makeDepositResponse = new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK()
        ).post(deposit);

        //проверка: номер счета, баланс и что массив с транзакциями не пустой
        soflty.assertThat(makeDepositResponse.getId()).isEqualTo(accountId);
        soflty.assertThat(makeDepositResponse.getBalance()).isEqualTo(MaxSumsForDepositAndTransactions.DEPOSIT.getMax());
        soflty.assertThat(makeDepositResponse.getTransactions()).isNotEmpty();

        //проверка 2: в ответе из запроса MakeDeposit среди транзакций есть транзакция с нужной суммой и др параметры

        //Найдем транзакцию, подходящую под наши требования: сумма, тип транзакции и счет зачисления
        Transaction targetTransaction = makeDepositResponse.getTransactions().stream()
                .filter(t -> t.getAmount().equals(deposit.getBalance()))//баланс такой же как пополнили
                .filter(t -> t.getRelatedAccountId().equals(deposit.getId())) //счет тот же
                .filter(t -> t.getType().equals(TransactionType.DEPOSIT.getMessage()))
                .findAny()
                .orElseThrow(() -> new AssertionError("Такой транзакции не существует"));


        //проверка 3. Запросим отдельно все транзакции по конкретному счету и будем искать нужную
        List<GetAccountTransactionsResponse> usersTransactions = UserSteps.getsAccountTransaction(newUser, accountId);

        //Смотрим, что среди транзакций есть та, которая подходит под требования: нужный баланс, нужный тип транзакции и нужный id
        usersTransactions.stream()
                .filter(t -> t.getAmount().equals(MaxSumsForDepositAndTransactions.DEPOSIT.getMax()))
                .filter(t -> t.getType().equals(TransactionType.DEPOSIT.getMessage()))
                .filter(t -> t.getRelatedAccountId().equals(accountId))
                .findAny()
                .orElseThrow(() -> new AssertionError("Нужная транзакция не найдена"));

        //Пост условие: удалить созданного юзера
        AdminSteps.deletesUser(newUser);
    }
}
