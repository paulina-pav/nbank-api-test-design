package apisenior.onlyapi.createacc;

import api.models.CreateAnAccountResponse;
import api.models.CreatedUser;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import org.junit.jupiter.api.Test;

public class UserCreatesAccountNegativeTest extends BaseTest {
    @Test
    public void unauthUserCantCreateAccount(){
        CreatedUser user = createUser();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.ACCOUNTS,
                ResponseSpecs.requestReturnsUnauthorized()
        ).post(null);

    }

    @Test
    public void adminCantCreateAccount(){
        CreatedUser user = createUser();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ACCOUNTS,
                ResponseSpecs.requestReturnsForbidden()
        ).post(null);
    }
}
