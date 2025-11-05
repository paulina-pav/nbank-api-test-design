package Tests.negatives;

import Requests.*;
import Specs.RequestSpecs;
import Specs.ResponseSpecs;
import Tests.BaseTest;
import generators.DataGenerator;
import generators.UserRole;
import models.DeleteByUserId;
import models.GetCustomerProfileResponse;
import models.NewUserRequest;
import models.UserChangeName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static generators.ErrorMessage.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY;

public class UserChangeNameNegativeTest extends BaseTest {

     /*
    Тест-кейсы этого файла:

    ### Тест-кейс 1: Юзер использует в качестве имени два слова, состоящие из букв, разделенные 2 пробелами
### Тест-кейс 2: Юзер использует в качестве имени одно слово из букв без пробела
### Тест-кейс 3: Юзер использует в качестве имени только 1 пробел
### Тест-кейс 4: Юзер использует в качестве имени не-буквы (цифры, спец.символы)
### Тест-кейс 5: Юзер использует в качестве имени 3 пробела
     */


    public static Stream<Arguments> invalidNames(){
        return Stream.of(

                Arguments.of("Polina  Polina", NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage()),
               Arguments.of("Polina", NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage()),
                Arguments.of(" ", NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage()),
                Arguments.of("   ", NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage()),
                Arguments.of("0", NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage())
        );
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    public void userChangeName(String invalidName, String expectedErrorMessage) {
        NewUserRequest newUser = NewUserRequest.builder()
                .username(DataGenerator.getUserName())
                .password(DataGenerator.getUserPassword())
                .role(UserRole.USER.toString())
                .build();
        new CreateNewUserRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.entityWasCreated())
                .post(newUser);

//пытаемся сменить имя
        String actualErrorMessage = new UserChangeNameRequester(
                RequestSpecs.authAsUser(newUser.getUsername(),newUser.getPassword()), ResponseSpecs.BadRequest())
                .put(new UserChangeName.Builder().setName(invalidName).build()).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage); //сверим что сообщение об ошибке правильное

//теперь проверим, что имя все еще null и не сменилось на неправильное
        GetCustomerProfileResponse getCustomerProfileAfter =
                new GetCustomerProfileRequester(
                        RequestSpecs.authAsUser(
                                newUser.getUsername(),
                                newUser.getPassword()
                        ),
                        ResponseSpecs.isOk()
                ).get().extract().as(GetCustomerProfileResponse.class);
        //Проверим юзернейм id имя и тд ПОСЛЕ смены имени
        soflty.assertThat(getCustomerProfileAfter.getName()).isNull(); //имя все еще нулл тк не сменилось
        soflty.assertThat(getCustomerProfileAfter.getUsername()).isEqualTo(newUser.getUsername()); //а юзернейм такой же
        soflty.assertThat(getCustomerProfileAfter.getPassword()).isNotNull();
        soflty.assertThat(getCustomerProfileAfter.getRole()).isEqualTo(newUser.getRole());



        //удалим всех
        List<Integer> userIds = new GetAllUsersRequester(
                RequestSpecs.adminAuth(),
                ResponseSpecs.isOk()
        ).get().extract().jsonPath().getList("id",  Integer.class);

        for (Integer id : userIds){
            new DeleteUserByIdRequester(RequestSpecs.adminAuth(), ResponseSpecs.isOk()).delete(new DeleteByUserId(id));

        }

    }

}
