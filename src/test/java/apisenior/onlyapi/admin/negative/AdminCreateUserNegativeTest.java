package apisenior.onlyapi.admin.negative;

import api.generators.ErrorMessage;
import api.generators.RandomHeaderGenerator;
import api.generators.RandomModelGenerator;
import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class AdminCreateUserNegativeTest extends BaseTest {//готово
    @Test
    public void adminCantCreateUserWithUnappropriatedUsername() { //done
        NewUserRequest userRequest =
                RandomModelGenerator.generate(NewUserRequest.class);

        userRequest.setRole(RandomHeaderGenerator.generateHeader());//просто сделаем роль рандомным вне требований

        CreateUserNegativeResponse response = new ValidatedCrudRequester<CreateUserNegativeResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER_NEGATIVE,
                ResponseSpecs.requestReturnsBadRequest()
        ).post(userRequest);

        //сама успешная десериализация уже означает, что Jackson нашел ключ "role" и записал его в поле role.
        soflty.assertThat(response.getRole().getFirst()).isEqualTo(ErrorMessage.ROLE_MUST_BE_EATHER_ADMIN_OR_USER.getMessage());


        boolean isUserCreated = AdminSteps.checkIfUserExistedByUsername(userRequest);

        soflty.assertThat(isUserCreated).isFalse();

        AdminSteps.deleteAllUsers();
    }

    @Test
    public void unknownUserCantCreateUser() { //cделано
        NewUserRequest userRequest =
                RandomModelGenerator.generate(NewUserRequest.class);


        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authWithRawHeader(RandomHeaderGenerator.generateHeader()),
                Endpoint.ADMIN_USER,
                ResponseSpecs.unauthorized()
        ).post(userRequest).extract().asString();

        soflty.assertThat(actualErrorMessage).isEmpty();
        soflty.assertThat(AdminSteps.getAllUsers()).isEmpty();

        AdminSteps.deleteAllUsers();
    }


        @Test
    public void userIsForbiddenToCreateUser() {

        CreatedUser user = createUser();

        NewUserRequest userRequest =
                RandomModelGenerator.generate(NewUserRequest.class);


            ForbiddenResponse response = new ValidatedCrudRequester<ForbiddenResponse>(
                    RequestSpecs.authAsUser(user.getRequest().getUsername(), user.getRequest().getPassword()),
                    Endpoint.ADMIN_USER_FORBIDDEN,
                    ResponseSpecs.requestReturnsForbidden()
            ).post(userRequest);


            soflty.assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_FORBIDDEN);
            soflty.assertThat(response.getPath())
                    .isEqualTo("/"+ CrudRequester.getApiVersionForTest() + Endpoint.ADMIN_USER_FORBIDDEN.getUrl());
            soflty.assertThat(response.getError()).isEqualTo(ErrorMessage.FORBIDDEN.getMessage());


            boolean isUserCreated = AdminSteps.checkIfUserExistedByUsername(userRequest);
            soflty.assertThat(isUserCreated).isFalse();


            //AdminSteps.deleteAllUsers();

    }
}
