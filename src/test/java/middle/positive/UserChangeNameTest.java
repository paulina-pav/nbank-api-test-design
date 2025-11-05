package Tests.positive;

import Requests.*;
import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import Tests.BaseTest;
import generators.DataGenerator;
import generators.ServiceMessages;
import generators.UserRole;
import models.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;


public class UserChangeNameTest extends BaseTest {

    @Test
    public void authUserChangeNameTest() {
         //создаем юзера
         NewUserRequest newUser = NewUserRequest.builder()
                 .username(DataGenerator.getUserName())
                 .password(DataGenerator.getUserPassword())
                 .role(UserRole.USER.toString())
                 .build();

         new CreateNewUserRequester(
                 RequestSpecs.adminAuth(),
                 ResponseSpecs.entityWasCreated())
                 .post(newUser);

         //Вызов getCustomerProfile. То, как юзер выглядит ДО переименования: id, username, name и роль
         GetCustomerProfileResponse getCustomerProfileBefore =
                 new GetCustomerProfileRequester(
                         RequestSpecs.authAsUser(
                                 newUser.getUsername(),
                                 newUser.getPassword()
                         ),
                         ResponseSpecs.isOk()
                 ).get().extract().as(GetCustomerProfileResponse.class);
    //Проверим юзернейм id имя и тд ДО смены имени
        soflty.assertThat(getCustomerProfileBefore.getName()).isNull(); //имя нулл
        soflty.assertThat(getCustomerProfileBefore.getUsername()).isEqualTo(newUser.getUsername());
        soflty.assertThat(getCustomerProfileBefore.getPassword()).isNotNull();
        soflty.assertThat(getCustomerProfileBefore.getRole()).isEqualTo(newUser.getRole());


         //меняем имя
        UserChangeName changedName = new UserChangeName.Builder().setName(DataGenerator.getName()).build();
        UserChangeNameResponse userChangeNameResponse = new UserChangeNameRequester(
                RequestSpecs.authAsUser(newUser.getUsername(),newUser.getPassword()), ResponseSpecs.isOk())
                .put(changedName)
                .extract()
                .as(UserChangeNameResponse.class);
        soflty.assertThat(userChangeNameResponse.getCustomer().getUsername()).isEqualTo(newUser.getUsername());
        soflty.assertThat(userChangeNameResponse.getCustomer().getPassword()).isNotNull();
        soflty.assertThat(userChangeNameResponse.getCustomer().getName()).isEqualTo(changedName.getName());
        soflty.assertThat(userChangeNameResponse.getCustomer().getRole()).isEqualTo(newUser.getRole());
        soflty.assertThat(userChangeNameResponse.getMessage()).isEqualTo(ServiceMessages.PROFILE_UPDATED_SUCCESSFULLY.getMessage());
        soflty.assertThat(userChangeNameResponse.getCustomer().getId()).isEqualTo(getCustomerProfileBefore.getId());

         //Вызов getCustomerProfile. То, как юзер выглядит ДО переименования: id, username, name и роль
         GetCustomerProfileResponse getCustomerProfileAfter =
                 new GetCustomerProfileRequester(
                         RequestSpecs.authAsUser(
                                 newUser.getUsername(),
                                 newUser.getPassword()
                         ),
                         ResponseSpecs.isOk()
                 ).get().extract().as(GetCustomerProfileResponse.class);
         //Проверим юзернейм id имя и тд ПОСЛЕ смены имени
         soflty.assertThat(getCustomerProfileAfter.getName()).isEqualTo(changedName.getName()); //имя новое
         soflty.assertThat(getCustomerProfileAfter.getUsername()).isEqualTo(newUser.getUsername());
         soflty.assertThat(getCustomerProfileAfter.getPassword()).isNotNull();
         soflty.assertThat(getCustomerProfileAfter.getRole()).isEqualTo(newUser.getRole());


        //вернем id всех юзеров
        List<Integer> ids = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().jsonPath().getList("id",  Integer.class);

        //удалим всех
        for (Integer id : ids){
            String successMessage = new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk())
                    .delete(new DeleteByUserId(id))
                    .extract().asString();

            String expected = String.format("User with ID %d deleted successfully.", id);
            soflty.assertThat(successMessage).isEqualTo(expected);

        }

     }

}
