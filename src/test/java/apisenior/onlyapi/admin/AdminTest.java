package apisenior.onlyapi.admin;

import api.models.CreatedUser;
import api.models.DeleteByUserIdResponse;
import api.models.NewUserRequest;
import api.models.UserLoginAuthRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import org.junit.jupiter.api.Test;

public class AdminTest extends BaseTest {

    @Test
    public void adminCantCreateUser400() {

        NewUserRequest user = NewUserRequest.builder()
                .role("USER")
                .password("123")
                .username("a")
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(user).extract().asString();
    }

    @Test
    public void adminCantCreateUser403() {
        CreatedUser newUser = createUser();


        NewUserRequest user = NewUserRequest.builder()
                .role("USER")
                .password("123")
                .username("a")
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(newUser.getRequest().getUsername(), newUser.getRequest().getPassword()),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsForbidden()
        ).post(user).extract().asString();
    }

    @Test
    public void adminCantCreateUser401() {

        NewUserRequest user = NewUserRequest.builder()
                .role("USER")
                .password("123")
                .username("a")
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authWithRawHeader("f"),
                Endpoint.ADMIN_USER,
                ResponseSpecs.unauthorized()
        ).post(user).extract().asString();
    }


    //логин юзером - переместить
//    @Test
//    public void login401() {
//        UserLoginAuthRequest request = UserLoginAuthRequest.builder()
//                .username("aa")
//                .password("bbb")
//                .build();
//
//        new CrudRequester(
//                RequestSpecs.unauthSpec(),
//                Endpoint.LOGIN,
//                ResponseSpecs.unauthorized()
//
//        ).post(request);
//
//    }

    @Test
    public void deleteuser401(){

        CreatedUser newUser = createUser();


        String successMessage = new ValidatedCrudRequester<DeleteByUserIdResponse>(
                RequestSpecs.authWithRawHeader("aaaa"),
                Endpoint.DELETE_USER_BY_ID,
                ResponseSpecs.unauthorized()
        ).delete(newUser.getResponse().getId());
    }
    @Test
    public void deleteuser403(){

        CreatedUser newUser = createUser();


        String successMessage = new ValidatedCrudRequester<DeleteByUserIdResponse>(
                RequestSpecs.authAsUser(newUser.getRequest().getUsername(), newUser.getRequest().getPassword()),
                Endpoint.DELETE_USER_BY_ID,
                ResponseSpecs.requestReturnsForbidden()
        ).delete(newUser.getResponse().getId());
    }

    @Test
    public void deleteuserok(){

        CreatedUser newUser = createUser();


        String successMessage = new ValidatedCrudRequester<DeleteByUserIdResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.DELETE_USER_BY_ID,
                ResponseSpecs.requestReturnsOK()
        ).delete(newUser.getResponse().getId());
        //исправить в степах
    }

    @Test
    public void deleteuser404(){

      //  CreatedUser newUser = createUser();


        String successMessage = new ValidatedCrudRequester<DeleteByUserIdResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.DELETE_USER_BY_ID,
                ResponseSpecs.notFound()
        ).delete(100555);
        //исправить в степах
    }


}
