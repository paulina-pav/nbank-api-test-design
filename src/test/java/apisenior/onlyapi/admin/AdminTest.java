package apisenior.onlyapi.admin;

import api.models.CreatedUser;
import api.models.NewUserRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import org.junit.jupiter.api.Test;

public class AdminTest extends BaseTest {

    @Test
    public void adminCantCreateUser400(){

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
    public void adminCantCreateUser403(){
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
    public void adminCantCreateUser401(){

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
}
