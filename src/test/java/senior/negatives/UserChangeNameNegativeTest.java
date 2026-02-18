package senior.negatives;


import api.comparison.ModelAssertions;
import api.models.GetCustomerProfileResponse;
import api.models.UserChangeNameRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.UserSteps;
import api.models.CreatedUser;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static api.generators.ErrorMessage.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY;


public class UserChangeNameNegativeTest extends senior.BaseTest {


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

    @DisplayName("Юзер не может сменить имя, используя невалидное имя")
    @ParameterizedTest
    @MethodSource("invalidNames")
    public void userChangeName(String invalidName, String expectedErrorMessage) {

        CreatedUser newUser = createUser();

        //неправильное имя
        UserChangeNameRequest invalidNameForUser = UserChangeNameRequest.builder()
                .name(invalidName)
                .build();

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(newUser.getRequest().getUsername(), newUser.getRequest().getPassword()),
                Endpoint.UPDATE_CUSTOMER_NAME,
                ResponseSpecs.requestReturnsBadRequest()
        ).put(invalidNameForUser).extract().asString();

        soflty.assertThat(actualErrorMessage).isEqualTo(expectedErrorMessage); //сверим что сообщение об ошибке правильное

        //теперь проверим, что имя все еще null и не сменилось на неправильное
        GetCustomerProfileResponse getCustomerProfileAfter = UserSteps.getsProfile(newUser.getRequest());
        ModelAssertions.assertThatModels(newUser.getResponse(), getCustomerProfileAfter).match();

    }
}

