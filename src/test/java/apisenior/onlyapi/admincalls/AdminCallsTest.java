package apisenior.onlyapi.admincalls;

import api.models.CreatedUser;
import api.models.DeleteByUserIdResponse;
import api.models.User;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AdminCallsTest {

    @Test
    public void adminCanGetUsers(){
        CreatedUser user = AdminSteps.createUser();

        List<User> users = new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.GET_ALL_USER,
                ResponseSpecs.requestReturnsOK()
        ).get()
                .extract()
                .as(new TypeRef<List<User>>() {
                });
    }

    @Test
    public void adminDeletesUser(){
        CreatedUser user = AdminSteps.createUser();
        String successMessage = new ValidatedCrudRequester<DeleteByUserIdResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.DELETE_USER_BY_ID,
                ResponseSpecs.requestReturnsOK()
        ).delete(user.getResponse().getId());
    }
}
