package api.requests.steps;

import api.generators.MaxSumsForDepositAndTransactions;
import api.generators.RandomModelGenerator;
import api.models.*;
import io.restassured.common.mapper.TypeRef;
import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class UserSteps {
    public static GetCustomerProfileResponse getsProfile(NewUserRequest user) {
        GetCustomerProfileResponse customerProfile = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()
        ).get();
        return customerProfile;
    }

    public static UserChangeNameResponse сhangesName(NewUserRequest user) {
        UserChangeNameRequest changedName = RandomModelGenerator.generate(UserChangeNameRequest.class);

        UserChangeNameResponse userChangeNameResponse = new ValidatedCrudRequester<UserChangeNameResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.UPDATE_CUSTOMER_NAME,
                ResponseSpecs.requestReturnsOK())
                .put(changedName);
        return userChangeNameResponse;
    }

    public static CreateAnAccountResponse createsAccount(NewUserRequest user) {

        CreateAnAccountResponse createAnAccResponse = new ValidatedCrudRequester<CreateAnAccountResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated()
        ).post(null);
        return createAnAccResponse;
    }

    public static MakeDepositResponse makesDeposit(Long accountId, NewUserRequest newUser) {

        MakeDepositRequest deposit = MakeDepositRequest.builder()
                .id(accountId)
                .balance(MaxSumsForDepositAndTransactions.DEPOSIT.getMax())
                .build();

        MakeDepositResponse makeDepositResponse = new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK()
        ).post(deposit);

        return makeDepositResponse;
    }


    public static MakeDepositResponse makesDepositX2(Long accountId, NewUserRequest newUser) {

       MakeDepositResponse makeDepositResponse = null;

        for (int i = 0; i <= 1; i++) {
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
    }


    public static List<GetCustomerAccountResponse> getsAccounts(NewUserRequest newUser) {
        List<GetCustomerAccountResponse> accounts = new CrudRequester(
                RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK()
        ).get()
                .extract()
                .as(new TypeRef<List<GetCustomerAccountResponse>>() {
                });

        return accounts;
    }

    public static List<GetAccountTransactionsResponse> getsAccountTransaction(NewUserRequest newUser, Long id) {

        List<GetAccountTransactionsResponse> transactionsByAcc = new CrudRequester(
                RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                Endpoint.GET_ACCOUNT_TRANSACTION,
                ResponseSpecs.requestReturnsOK()
        ).get(id)
                .extract().as(new TypeRef<List<GetAccountTransactionsResponse>>() {
                });
        return transactionsByAcc;
    }

}
