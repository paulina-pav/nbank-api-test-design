package apisenior.onlyapi.admin;

import api.generators.ServiceMessageHelpMethods;
import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminPositiveTest extends BaseTest {


    @Test
    public void adminCanDeleteUserSuccessfully() {

        CreatedUser newUser = createUser();

        String successMessage = new ValidatedCrudRequester<DeleteByUserIdSuccessfulResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.DELETE_USER_BY_ID,
                ResponseSpecs.requestReturnsOK()
        ).delete(newUser.getResponse().getId());

        soflty.assertThat(successMessage).isEqualTo(ServiceMessageHelpMethods.createDeleteUserSuccessfulMessage(newUser.getResponse().getId()));

        boolean result = AdminSteps.checkIfUserAlreadyDeleted(newUser);
        soflty.assertThat(result).isFalse();

        boolean isUserExisted = AdminSteps.checkIfUserExistedByUsername(newUser.getRequest());
        soflty.assertThat(isUserExisted).isFalse();
    }


    @Test
    public void adminCanGetAllUsersSuccessfully() {

        CreatedUser newUser = createUser();

        NewUserResponse[] responses = new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.GET_ALL_USER,
                ResponseSpecs.requestReturnsOK()
        ).get()
                .extract()
                .as(NewUserResponse[].class);

        List<NewUserResponse> users = Arrays.asList(responses);

        boolean isUserCreated = AdminSteps.checkIfUserExistedByUsername(newUser.getRequest());

        soflty.assertThat(isUserCreated).isTrue();
    }
}
