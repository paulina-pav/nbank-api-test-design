package senior.positive;

import api.comparison.ModelAssertions;
import api.generators.RandomModelGenerator;
import api.generators.ServiceMessages;
import api.models.GetCustomerProfileResponse;
import api.models.UserChangeNameRequest;
import api.models.UserChangeNameResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.UserSteps;
import api.requests.steps.result.CreatedUser;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import senior.BaseTest;

public class UserChangesNameTest extends BaseTest {


//Тест-кейс: авторизованный юзер меняет имя

    @Test
    @DisplayName("Юзер может сменить имя")
    public void authUserChangeNameTest() {

        CreatedUser newUser = createUser();

        //как юзер выглядит ДО переименования: id, username, name и роль
        GetCustomerProfileResponse getCustomerProfileBefore = UserSteps.getsProfile(newUser.getRequest());

        ModelAssertions.assertThatModels(newUser.getResponse(), getCustomerProfileBefore).match();
        soflty.assertThat(getCustomerProfileBefore.getName()).isNull(); //имя нулл


        UserChangeNameRequest changedName = RandomModelGenerator.generate(UserChangeNameRequest.class);

        UserChangeNameResponse userChangeNameResponse = new ValidatedCrudRequester<UserChangeNameResponse>(
                RequestSpecs.authAsUser(newUser.getRequest().getUsername(), newUser.getRequest().getPassword()),
                Endpoint.UPDATE_CUSTOMER_NAME,
                ResponseSpecs.requestReturnsOK()
        ).put(changedName);

        ModelAssertions.assertThatModels(newUser.getResponse(), userChangeNameResponse.getCustomer()).match();
        soflty.assertThat(userChangeNameResponse.getCustomer().getName()).isEqualTo(changedName.getName());
        soflty.assertThat(userChangeNameResponse.getMessage()).isEqualTo(ServiceMessages.PROFILE_UPDATED_SUCCESSFULLY.getMessage());


        //как юзер выглядит после переименования: id, username, name и роль
        GetCustomerProfileResponse getCustomerProfileAfter = UserSteps.getsProfile(newUser.getRequest());

        ModelAssertions.assertThatModels(newUser.getResponse(), getCustomerProfileAfter).match();
        soflty.assertThat(getCustomerProfileAfter.getName()).isEqualTo(changedName.getName()); //имя новое
    }
}

