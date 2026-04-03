package apisenior.onlyapi.admincalls;

import api.models.CreatedUser;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.junit.jupiter.api.Test;

public class AdminCallsNegativeTest {

    @Test
    public void unauthAdminCantGetUsers() {
        CreatedUser user = AdminSteps.createUser();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.GET_ALL_USER,
                ResponseSpecs.requestReturnsUnauthorized()
        ).get();
    }

    @Test
    public void userCantGetUsers() {
        CreatedUser user = AdminSteps.createUser();

        new CrudRequester(
                RequestSpecs.authAsUser(user.getRequest().getUsername(), user.getRequest().getPassword()),
                Endpoint.GET_ALL_USER,
                ResponseSpecs.requestReturnsForbidden()
        ).get();
    }

    @Test
    public void adminCantDeletedAlreadyDeletedUser() {
        CreatedUser user = AdminSteps.createUser();
        AdminSteps.deletesUser(user);

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.DELETE_USER_BY_ID,
                ResponseSpecs.requestReturnsNotFound()
        ).delete(user.getResponse().getId());
    }

    @Test
    public void unAuthcAntDeleteUser() {
        CreatedUser user = AdminSteps.createUser();
        AdminSteps.deletesUser(user);

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.DELETE_USER_BY_ID,
                ResponseSpecs.requestReturnsUnauthorized()
        ).delete(user.getResponse().getId());
    }

    @Test
    public void userCantDeleteUser() {
        CreatedUser user = AdminSteps.createUser();

        new CrudRequester(
                RequestSpecs.authAsUser(user.getRequest().getUsername(), user.getRequest().getPassword()),
                Endpoint.DELETE_USER_BY_ID,
                ResponseSpecs.requestReturnsForbidden()
        ).delete(user.getResponse().getId());
    }
}
