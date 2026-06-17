package apisenior.onlyapi.admin.negative;

import api.generators.ErrorMessage;
import api.generators.RandomHeaderGenerator;
import api.models.CreatedUser;
import api.models.ForbiddenResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class GetAllUsersNegativeTest extends BaseTest {

    @Test
    public void userIsForbiddenToGetAllUsers() {
        CreatedUser newUser = createUser();


        ForbiddenResponse response = new ValidatedCrudRequester<ForbiddenResponse>(
                RequestSpecs.authAsUser(newUser.getRequest().getUsername(), newUser.getRequest().getPassword()),
                Endpoint.GET_ALL_USER_FORBIDDEN,
                ResponseSpecs.requestReturnsForbidden()
        ).get();

        soflty.assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_FORBIDDEN);
        soflty.assertThat(response.getPath())
                .isEqualTo("/"+ CrudRequester.getApiVersionForTest() +
                        Endpoint.ADMIN_USER_FORBIDDEN.getUrl());
        soflty.assertThat(response.getError()).isEqualTo(ErrorMessage.FORBIDDEN.getMessage());

    }

    @Test
    public void unknownUserCantGetAllUsers() {
        //401
        CreatedUser newUser = createUser();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authWithRawHeader(RandomHeaderGenerator.generateHeader()),
                Endpoint.GET_ALL_USER,
                ResponseSpecs.unauthorized()
        ).get().toString();

        soflty.assertThat(actualErrorMessage).isEmpty();
    }
}
