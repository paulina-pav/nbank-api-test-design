package senior.positive;

import api.generators.RandomModelGenerator;
import api.generators.ServiceMessages;
import api.models.GetCustomerProfileResponse;
import api.models.NewUserRequest;
import api.models.UserChangeNameRequest;
import api.models.UserChangeNameResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.junit.jupiter.api.Test;

public class UserChangesNameTest extends Tests.BaseTest {


//Тест-кейс: авторизованный юзер меняет имя

    @Test
    public void authUserChangeNameTest() {

        //Создаем юзера
        NewUserRequest newUser = AdminSteps.createUser();

        //Смотрим, как юзер выглядит ДО переименования: id, username, name и роль
        GetCustomerProfileResponse getCustomerProfileBefore = UserSteps.getsProfile(newUser);
        soflty.assertThat(getCustomerProfileBefore.getName()).isNull(); //имя нулл
        soflty.assertThat(getCustomerProfileBefore.getUsername()).isEqualTo(newUser.getUsername());
        soflty.assertThat(getCustomerProfileBefore.getPassword()).isNotNull();
        soflty.assertThat(getCustomerProfileBefore.getRole()).isEqualTo(newUser.getRole());

        //Шаг 2: меняем имя
        UserChangeNameRequest changedName = RandomModelGenerator.generate(UserChangeNameRequest.class);

        UserChangeNameResponse userChangeNameResponse = new ValidatedCrudRequester<UserChangeNameResponse>(
                RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                Endpoint.UPDATE_CUSTOMER_NAME,
                ResponseSpecs.requestReturnsOK()
        ).put(changedName);
        soflty.assertThat(userChangeNameResponse.getCustomer().getUsername()).isEqualTo(newUser.getUsername());
        soflty.assertThat(userChangeNameResponse.getCustomer().getPassword()).isNotNull();
        soflty.assertThat(userChangeNameResponse.getCustomer().getName()).isEqualTo(changedName.getName());
        soflty.assertThat(userChangeNameResponse.getCustomer().getRole()).isEqualTo(newUser.getRole());
        soflty.assertThat(userChangeNameResponse.getMessage()).isEqualTo(ServiceMessages.PROFILE_UPDATED_SUCCESSFULLY.getMessage());
        soflty.assertThat(userChangeNameResponse.getCustomer().getId()).isEqualTo(getCustomerProfileBefore.getId());

        //Вызов getCustomerProfile. То, как юзер выглядит после переименования: id, username, name и роль
        GetCustomerProfileResponse getCustomerProfileAfter = UserSteps.getsProfile(newUser);
        soflty.assertThat(getCustomerProfileAfter.getName()).isEqualTo(changedName.getName()); //имя новое
        soflty.assertThat(getCustomerProfileAfter.getUsername()).isEqualTo(newUser.getUsername());
        soflty.assertThat(getCustomerProfileAfter.getPassword()).isNotNull();
        soflty.assertThat(getCustomerProfileAfter.getRole()).isEqualTo(newUser.getRole());


        //Удалить юзера, которого создали
        AdminSteps.deletesUser(newUser);
    }
}

