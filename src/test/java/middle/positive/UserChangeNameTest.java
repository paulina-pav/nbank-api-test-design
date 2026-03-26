package middle.positive;

import Requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import Tests.BaseTest;
import generators.DataGenerator;
import generators.ServiceMessages;
import generators.UserRole;
import io.restassured.common.mapper.TypeRef;
import models.*;
import org.junit.jupiter.api.Test;
import java.util.List;



public class UserChangeNameTest extends BaseTest {

    @Test
    public void authUserChangeNameTest() {
         //Предусловие, шаг 1: создаем юзера
         NewUserRequest newUser = NewUserRequest.builder()
                 .username(DataGenerator.getUserName())
                 .password(DataGenerator.getUserPassword())
                 .role(UserRole.USER.toString())
                 .build();

         NewUserResponse newUserResponse = new CreateNewUserRequester(
                 RequestSpecs.adminAuth(),
                 ResponseSpecs.entityWasCreated())
                 .post(newUser).extract().as(NewUserResponse.class);

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


         //Шаг 2: меняем имя
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

         //Вызов getCustomerProfile. То, как юзер выглядит после переименования: id, username, name и роль
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

         //Пост условие шаг 3: удалить созданного юзера

        //В этом тесте создали только 1 юзера, его и удалим по id. Возьмем его из newUserResponse
        String successMessage = new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk())
                .delete(new DeleteByUserId(newUserResponse.getId()))
                .extract().asString();

        String expected = String.format("User with ID %d deleted successfully.", newUserResponse.getId());
        soflty.assertThat(successMessage).isEqualTo(expected);

        //Убедимся, что такого юзера нет. Админ вызывает getAllUsers, сразу положим в лист
        List<GetAllUsersResponse> users = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().as(new TypeRef<List<GetAllUsersResponse>>() {});

        //вытащим их id в лист
        List<Integer> userIds = users.stream()
                .map(GetAllUsersResponse::getId)
                .toList();

        //проверим, что такого id нет
        soflty.assertThat(userIds)
                .as("удалённый id не должен быть среди пользователей")
                .doesNotContain(newUserResponse.getId());

     }

}
