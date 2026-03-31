package api.requests.skelethon;

import api.models.NewUserRequest;
import api.models.NewUserResponse;
import api.models.GetAllUsersRequest;
import api.models.GetAllUsersResponse;
import api.models.UserLoginAuthRequest;
import api.models.UserLoginAuthResponse;
import api.models.BaseModel;
import api.models.CreateAnAccountResponse;
import api.models.GetCustomerProfileResponse;
import api.models.GetCustomerAccountResponse;
import api.models.UserChangeNameRequest;
import api.models.UserChangeNameResponse;
import api.models.DeleteByUserIdRequest;
import api.models.DeleteByUserIdResponse;
import api.models.MakeDepositRequest;
import api.models.MakeDepositResponse;
import api.models.GetAccountTransactionsRequest;
import api.models.GetAccountTransactionsResponse;
import api.models.TransferMoneyResponse;
import api.models.TransferMoneyRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;


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
