package apisenior.db.makesdeposit;


import api.comparison.ModelAssertions;
import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.TransactionType;
import api.models.CreatedUser;
import api.models.MakeDepositRequest;
import api.models.MakeDepositResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
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
import org.junit.jupiter.api.Test;


public class UserMakesDepositTest extends BaseTest {

    //Тест-кейс : Авторизованный юзер делает депозит на свой счет

    @DisplayName("Юзер делает депозит")
    @Test
    @EnabledForBackend(BackendProfile.WITH_DATABASE_WITH_FIX)
    public void authUserMakesDeposit() {

        CreatedUser newUser = createUser();

        Long accountId = UserSteps.createsAccount(newUser.getRequest()).getId();
        Double balanceBefore = UserSteps.getBalance(newUser.getRequest(), accountId);

        //Юзер делает депозит. Пополним этот счет на максимальное значение для депозита
        MakeDepositRequest deposit = MakeDepositRequest.builder()
                .id(accountId)
                .balance(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .build();

        MakeDepositResponse makeDepositResponse = new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(newUser.getRequest().getUsername(), newUser.getRequest().getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK()
        ).post(deposit);

        //проверка: номер счета, баланс и что массив с транзакциями не пустой
        ModelAssertions.assertThatModels(deposit, makeDepositResponse).match();

        //проверка 2: в ответе из запроса MakeDeposit есть транзакция с нужной суммой и др параметры
        ModelAssertions.assertThatModels(makeDepositResponse, makeDepositResponse.getTransactions().get(0)).match();

        //Проверка 3: баланс счета изменился
        Double balanceAfter = UserSteps.getBalance(newUser.getRequest(), accountId);
        soflty.assertThat(balanceAfter).isEqualTo(balanceBefore + MaxSumsForDepositAndTransactions.DEPOSIT.getMax());

        //проверка 4. Запросим отдельно все транзакции по конкретному счету и будем искать нужную
        boolean isTransaction = UserSteps.findTransactionBySumByTransactionTypeByAccId(MaxSumsForDepositAndTransactions.DEPOSIT.getMax(),
                TransactionType.DEPOSIT.getMessage(), accountId, accountId, newUser.getRequest());
        soflty.assertThat(isTransaction).isTrue();


        TransactionDao transactionDao = DBSteps.findTransactionByTypeBySumByAccountIdByRelatedAccountId(TransactionType.TRANSFER_OUT.getMessage(),
                MaxSumsForDepositAndTransactions.TRANSACTION.getMax(), accountId, accountId
        );
        DaoAndModelAssertions.assertThat(makeDepositResponse, transactionDao);

        AccountDao accountDao = DBSteps.getAccountByUserIdAndBalance(newUser.getResponse().getId(), MaxSumsForDepositAndTransactions.DEPOSIT.getMax());
        DaoAndModelAssertions.assertThat(makeDepositResponse, accountDao);
    }
}
