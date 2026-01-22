package api.requests.skelethon;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import api.models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {

   ADMIN_USER(
            "/admin/users",
            NewUserRequest.class,
            NewUserResponse.class
    ),

    GET_ALL_USER(
            "/admin/users",
            GetAllUsersRequest.class,
            GetAllUsersResponse.class
    ),

    LOGIN(
            "/auth/login",
            UserLoginAuthRequest.class,
            UserLoginAuthResponse.class
    ),

    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAnAccountResponse.class
    ),
 CUSTOMER_PROFILE(
         "/customer/profile",
         BaseModel.class,
         GetCustomerProfileResponse.class
 ),
    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            GetCustomerAccountResponse.class
    ),
    UPDATE_CUSTOMER_NAME(
            "/customer/profile",
            UserChangeNameRequest.class,
            UserChangeNameResponse.class
    ),
    DELETE_USER_BY_ID(
            "admin/users/",
            DeleteByUserIdRequest.class,
            DeleteByUserIdResponse.class
    ),
    DEPOSIT(
            "/accounts/deposit",
            MakeDepositRequest.class,
            MakeDepositResponse.class
    ),
    GET_ACCOUNT_TRANSACTION(
            "/accounts/{id}/transactions",
            GetAccountTransactionsRequest.class,
            GetAccountTransactionsResponse.class
    ),
    TRANSFER(
            "accounts/transfer",
            TransferMoneyRequest.class,
            TransferMoneyResponse.class
    );
    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;

}
