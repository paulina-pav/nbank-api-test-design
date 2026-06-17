package apisenior.onlyapi.admin.negative;

import api.generators.ErrorMessage;
import api.generators.RandomHeaderGenerator;
import api.generators.ServiceMessageHelpMethods;
import api.models.CreatedUser;
import api.models.DeleteByUserIdSuccessfulResponse;
import api.models.ForbiddenResponse;
import api.models.User;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DeleteUserNegative extends BaseTest {
    @Test
    public void adminCantDeleteAlreadyDeletedUser() {//done

        CreatedUser deletedUser = AdminSteps.createAndDeleteUser();

        String successMessage = new ValidatedCrudRequester<DeleteByUserIdSuccessfulResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.DELETE_USER_BY_ID,
                ResponseSpecs.notFound()
        ).delete(deletedUser.getResponse().getId());

        soflty.assertThat(successMessage).isEqualTo(ServiceMessageHelpMethods.userNotFoundMessage(deletedUser.getResponse().getId()));


        boolean isUserExisted = AdminSteps.checkIfUserExistedByUsername(deletedUser.getRequest());
        soflty.assertThat(isUserExisted).isFalse();
    }



    @Test
    public void unknownUserCantDeleteUser() {

        CreatedUser newUser = createUser();

        String message = new CrudRequester(
                RequestSpecs.authWithRawHeader(RandomHeaderGenerator.generateHeader()),
                Endpoint.DELETE_USER_BY_ID,
                ResponseSpecs.unauthorized()
        ).delete(newUser.getResponse().getId()).extract().asString();

        soflty.assertThat(message).isEmpty();

        boolean isUserExisted = AdminSteps.checkIfUserExistedByUsername(newUser.getRequest());
        soflty.assertThat(isUserExisted).isTrue();

        //проверить что пользователь на месте

    }



    @Test
    public void userIsForbiddenToDeleteUser() {

        CreatedUser newUser = createUser();

        ForbiddenResponse response = new ValidatedCrudRequester<ForbiddenResponse>(
                RequestSpecs.authAsUser(newUser.getRequest().getUsername(), newUser.getRequest().getPassword()),
                Endpoint.DELETE_USER_BY_ID_FORBIDDEN,
                ResponseSpecs.requestReturnsForbidden()
        ).deleteAndGetResponseModel(newUser.getResponse().getId());

        soflty.assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_FORBIDDEN);
        soflty.assertThat(response.getPath())
                .isEqualTo("/"+ CrudRequester.getApiVersionForTest() +
                        Endpoint.ADMIN_USER_FORBIDDEN.getUrl() + "/" + newUser.getResponse().getId());
        soflty.assertThat(response.getError()).isEqualTo(ErrorMessage.FORBIDDEN.getMessage());

        boolean isUserCreated = AdminSteps.checkIfUserExistedByUsername(newUser.getRequest());

        soflty.assertThat(isUserCreated).isTrue();
  }
}
