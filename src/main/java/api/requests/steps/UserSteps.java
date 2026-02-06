package api.requests.steps;

import api.common.helpers.StepLogger;
import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.RandomModelGenerator;
import api.generators.TransactionType;
import api.models.*;

import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import io.restassured.common.mapper.TypeRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserSteps {
    public static GetCustomerProfileResponse getsProfile(NewUserRequest user) {

        return StepLogger.log("User" + user.getUsername() + " gets profile ", () -> {

            GetCustomerProfileResponse customerProfile = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                    RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                    Endpoint.CUSTOMER_PROFILE,
                    ResponseSpecs.requestReturnsOK()
            ).get();
            return customerProfile;
        });
    }

    public static UserChangeNameResponse сhangesName(NewUserRequest user) {
        UserChangeNameRequest changedName = RandomModelGenerator.generate(UserChangeNameRequest.class);

        return StepLogger.log("User " + user.getUsername() + "changes name " + changedName, () -> {
            UserChangeNameResponse userChangeNameResponse = new ValidatedCrudRequester<UserChangeNameResponse>(
                    RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                    Endpoint.UPDATE_CUSTOMER_NAME,
                    ResponseSpecs.requestReturnsOK())
                    .put(changedName);
            return userChangeNameResponse;
        });
    }

    public static CreateAnAccountResponse createsAccount(NewUserRequest user) {

        return StepLogger.log("User " + user.getUsername() + " creates a new account ", () -> {
            CreateAnAccountResponse createAnAccResponse = new ValidatedCrudRequester<CreateAnAccountResponse>(
                    RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                    Endpoint.ACCOUNTS,
                    ResponseSpecs.entityWasCreated()
            ).post(null);
            return createAnAccResponse;
        });
    }

    public static MakeDepositResponse makesDeposit(Long accountId, NewUserRequest newUser) {

        MakeDepositRequest deposit = MakeDepositRequest.builder()
                .id(accountId)
                .balance(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .build();

        return StepLogger.log("User " + newUser.getUsername() + " makes deposit " + deposit.getBalance() + " on " + accountId, () -> {
            MakeDepositResponse makeDepositResponse = new ValidatedCrudRequester<MakeDepositResponse>(
                    RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                    Endpoint.DEPOSIT,
                    ResponseSpecs.requestReturnsOK()
            ).post(deposit);

            return makeDepositResponse;
        });
    }


    public static MakeDepositResponse makesDepositX2(Long accountId, NewUserRequest newUser) {

        return StepLogger.log("User " + newUser.getUsername() + " makes deposit X2" + " on " + accountId, () -> {


            MakeDepositResponse makeDepositResponse = null;

            double balance = 0.0;

            for (int i = 0; i <= 1; i++) {
                MakeDepositRequest deposit = MakeDepositRequest.builder()
                        .id(accountId)
                        .balance(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                        .build();
                balance = balance + MaxSumsForDepositAndTransactions.DEPOSIT.getMax();

                makeDepositResponse = new ValidatedCrudRequester<MakeDepositResponse>(
                        RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                        Endpoint.DEPOSIT,
                        ResponseSpecs.requestReturnsOK()
                ).post(deposit);

            }
            return makeDepositResponse;
        });
    }

    public static MakeDepositResponse makesDepositX3(Long accountId, NewUserRequest newUser) {

        return StepLogger.log("User " + newUser.getUsername() + " makes deposit X3" + " on " + accountId, () -> {

            MakeDepositResponse makeDepositResponse = null;

            for (int i = 0; i <= 2; i++) {
                MakeDepositRequest deposit = MakeDepositRequest.builder()
                        .id(accountId)
                        .balance(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                        .build();

                makeDepositResponse = new ValidatedCrudRequester<MakeDepositResponse>(
                        RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                        Endpoint.DEPOSIT,
                        ResponseSpecs.requestReturnsOK()
                ).post(deposit);
            }

            return makeDepositResponse;
        });
    }

    public static List<GetCustomerAccountResponse> getsAccounts(NewUserRequest newUser) {

        return StepLogger.log("User " + newUser.getUsername() + " gets accounts ", () -> {

            List<GetCustomerAccountResponse> accounts = new CrudRequester(
                    RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                    Endpoint.CUSTOMER_ACCOUNTS,
                    ResponseSpecs.requestReturnsOK()
            ).get()
                    .extract()
                    .as(new TypeRef<List<GetCustomerAccountResponse>>() {
                    });

            return accounts;
        });
    }

    public static List<GetAccountTransactionsResponse> getsAccountTransaction(NewUserRequest newUser, Long id) {

        return StepLogger.log("User " + newUser.getUsername() + " gets transactions by  " + id, () -> {


            List<GetAccountTransactionsResponse> transactionsByAcc = new CrudRequester(
                    RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                    Endpoint.GET_ACCOUNT_TRANSACTION,
                    ResponseSpecs.requestReturnsOK()
            ).get(id)
                    .extract().as(new TypeRef<List<GetAccountTransactionsResponse>>() {
                    });
            return transactionsByAcc;
        });
    }


    public static Double getBalance(NewUserRequest user, Long accId) {

        return StepLogger.log("User " + user.getUsername() + " gets balance " + accId, () -> {


            //Запросим информацию о счетах после пополнения, чтобы зафиксировать их балансы до транзакции
            List<GetCustomerAccountResponse> allUserAccs = UserSteps.getsAccounts(user);

            //найдем нужный счет
            GetCustomerAccountResponse account = allUserAccs.stream()
                    .filter(a -> a.getId().equals(accId))
                    .findAny()
                    .orElseThrow(() -> new AssertionError("счета не существует"));

            return account.getBalance();
        });
    }

    public static boolean findTransactionBySumByTransactionTypeByAccId(Double sum, String type, Long currentAcc, Long relatedAcc, NewUserRequest user) {

        return StepLogger.log("User " + user.getUsername() + " finds a transaction by sum " + sum + ",  transaction type " + type + ", accountId " + currentAcc
                + ", related account " + relatedAcc, () -> {

            //запросили все транзакции по счету
            List<GetAccountTransactionsResponse> transactionsByAcc = new CrudRequester(
                    RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                    Endpoint.GET_ACCOUNT_TRANSACTION,
                    ResponseSpecs.requestReturnsOK()
            ).get(currentAcc)
                    .extract().as(new TypeRef<List<GetAccountTransactionsResponse>>() {
                    });

            //Смотрим, что среди транзакций есть та, которая подходит под требования: нужный баланс, нужный тип транзакции и нужный id
            Optional<GetAccountTransactionsResponse> foundTransaction = transactionsByAcc.stream()
                    .filter(t -> t.getAmount().equals(sum))
                    .filter(t -> t.getType().equals(type))
                    .filter(t -> t.getRelatedAccountId().equals(relatedAcc))
                    .findAny();

            boolean flag = false;
            if (foundTransaction.isPresent()) {
                flag = true;
            }
            return flag;
        });

    }

    public static List<CreateAnAccountResponse> createTwoAccounts(NewUserRequest user) {
        return StepLogger.log("User " + user.getUsername() + " creates two accounts ", () -> {

            List<CreateAnAccountResponse> newAccounts = new ArrayList<>();
            for (int i = 0; i <= 1; i++) {
                CreateAnAccountResponse account = UserSteps.createsAccount(user);
                newAccounts.add(account);
            }
            return newAccounts;
        });
    }
}
