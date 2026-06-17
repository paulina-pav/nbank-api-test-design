package api.requests.skelethon;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum Endpoint {

   ADMIN_USER(
            "admin/users",
            NewUserRequest.class,
            NewUserResponse.class
    ),
    ADMIN_USER_NEGATIVE(
            "admin/users",
            NewUserRequest.class,
            CreateUserNegativeResponse.class
    ),
    ADMIN_USER_FORBIDDEN(
            "admin/users",
            NewUserRequest.class,
            ForbiddenResponse.class
    ),

    GET_ALL_USER(
            "admin/users",
            BaseModel.class,
            BaseModel.class
    ),

    GET_ALL_USER_FORBIDDEN(
            "admin/users",
            GetAllUsersRequest.class,
            ForbiddenResponse.class
    ),

    LOGIN(
            "auth/login",
            UserLoginAuthRequest.class,
            UserLoginAuthResponse.class
    ),

    ACCOUNTS(
            "accounts",
            BaseModel.class,
            CreateAnAccountResponse.class
    ),
 CUSTOMER_PROFILE(
         "customer/profile",
         BaseModel.class,
         GetCustomerProfileResponse.class
 ),
    CUSTOMER_ACCOUNTS(
            "customer/accounts",
            BaseModel.class,
            GetCustomerAccountResponse.class
    ),
    UPDATE_CUSTOMER_NAME(
            "customer/profile",
            UserChangeNameRequest.class,
            UserChangeNameResponse.class
    ),
    DELETE_USER_BY_ID(
            "admin/users/{id}",
            DeleteByUserIdRequest.class,
            DeleteByUserIdSuccessfulResponse.class
    ),

    DELETE_USER_BY_ID_FORBIDDEN(
            "admin/users/{id}",
            DeleteByUserIdRequest.class,
            ForbiddenResponse.class
    ),

    DEPOSIT(
            "accounts/deposit",
            MakeDepositRequest.class,
            MakeDepositResponse.class
    ),
    GET_ACCOUNT_TRANSACTION(
            "accounts/{id}/transactions",
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
