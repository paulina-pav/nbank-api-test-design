package senior.negatives;


import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import Tests.BaseTest;
import api.models.NewUserRequest;
import api.models.GetCustomerProfileResponse;
import api.models.UserChangeNameRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static api.generators.ErrorMessage.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY;


public class UserChangeNameNegativeTest extends BaseTest {


   /* Тест-кейсы этого файла:
### Тест-кейс 1: Юзер использует в качестве имени два слова, состоящие из букв, разделенные 2 пробелами
### Тест-кейс 2: Юзер использует в качестве имени одно слово из букв без пробела
### Тест-кейс 3: Юзер использует в качестве имени только 1 пробел
### Тест-кейс 4: Юзер использует в качестве имени не-буквы (цифры, спец.символы)
### Тест-кейс 5: Юзер использует в качестве имени 3 пробела
     */

    public static Stream<Arguments> invalidNames() {
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

        //Предусловие шаг 1 - создать юзера
        NewUserRequest newUser = AdminSteps.createUser();

        //неправильное имя
        UserChangeNameRequest invalidNameForUser = UserChangeNameRequest.builder()
                .name(invalidName)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(newUser.getUsername(), newUser.getPassword()),
                Endpoint.UPDATE_CUSTOMER_NAME,
                ResponseSpecs.requestReturnsBadRequest()
        ).put(invalidNameForUser).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage); //сверим что сообщение об ошибке правильное

//теперь проверим, что имя все еще null и не сменилось на неправильное
        GetCustomerProfileResponse getCustomerProfileAfter = UserSteps.getsProfile(newUser);

        soflty.assertThat(getCustomerProfileAfter.getName()).isNull(); //имя все еще нулл тк не сменилось
        soflty.assertThat(getCustomerProfileAfter.getUsername()).isEqualTo(newUser.getUsername()); //юзернейм такой же
        soflty.assertThat(getCustomerProfileAfter.getPassword()).isNotNull();
        soflty.assertThat(getCustomerProfileAfter.getRole()).isEqualTo(newUser.getRole());

        //Удалить юзера
        AdminSteps.deletesUser(newUser);
    }
}
