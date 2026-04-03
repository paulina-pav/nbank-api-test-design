package apisenior.onlyapi.createacc;

import api.models.CreateAnAccountResponse;
import api.models.CreatedUser;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import org.junit.jupiter.api.Test;

public class UserCreatesAccountTest extends BaseTest {
    @Test
    public void userCanCreateAccount(){
        CreatedUser user = createUser();

        CreateAnAccountResponse createAnAccResponse = new ValidatedCrudRequester<CreateAnAccountResponse>(
                RequestSpecs.authAsUser(user.getRequest().getUsername(), user.getRequest().getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated()
        ).post(null);

    }
}
