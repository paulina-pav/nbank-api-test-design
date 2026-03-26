package negative.changename;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserChangeNameNegativeTests {

        /*
    Тест-кейсы этого файла:

    ### Тест-кейс 1: Юзер использует в качестве имени два слова, состоящие из букв, разделенные 2 пробелами
### Тест-кейс 2: Юзер использует в качестве имени одно слово из букв без пробела
### Тест-кейс 3: Юзер использует в качестве имени только 1 пробел
### Тест-кейс 4: Юзер использует в качестве имени не-буквы (цифры, спец.символы)
### Тест-кейс 5: Юзер использует в качестве имени 3 пробела
     */


    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }
    public static Stream<Arguments> invalidNames(){
        return Stream.of(

                Arguments.of("Polina  Polina", "Name must contain two words with letters only"),
                Arguments.of("Polina", "Name must contain two words with letters only"),
                Arguments.of(" ", "Name must contain two words with letters only"),
                Arguments.of("   ", "Name must contain two words with letters only"),
                Arguments.of("0", "Name must contain two words with letters only")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidNames")
    public void userChangeName(String invalidName, String errorMessage) {
        Map<String, Object> adminCreds = Map.of(
                "username", "admin",
                "password", "admin"
        );

        //Предусловие, шаг 1: админ получает токен
        String authAdminToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(adminCreds)
                .post("http://127.0.0.1:5000/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .extract()
                .header("Authorization");

        //Предусловие, шаг 2: авторизованный админ создает юзера. Немного провалидируем ответ
        //заведем переменную для логина и пароля юзера
        Map<String, Object> bodyToCreateUser = Map.of(
                "username", "kate1996",
                "password", "15101996Kate!",
                "role", "USER"
        );
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", authAdminToken)
                .body(bodyToCreateUser)
                .post("http://127.0.0.1:5000/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo("kate1996"))
                .body("password", Matchers.not(Matchers.equalTo("15101996Kate!")))
                .body("role", Matchers.equalTo("USER"));

        //Предусловие, шаг 3. Проверим, что юзер создался. Запишем айди юзера в переменную
        int userId = given()
                .header("Authorization", authAdminToken)
                .get("http://127.0.0.1:5000/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("[0].id");

        //подготовим данные для авторизации
        Map<String, Object> userAuthData = Map.of(
                "username", "kate1996",
                "password", "15101996Kate!"
        );
        //Предусловие, шаг 4: юзер авторизовался. Вытащим токен юзера в переменную
        String userAuthToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(userAuthData)
                .post("http://127.0.0.1:5000/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", Matchers.notNullValue())
                .extract()
                .header("Authorization");
        //юзер меняет имя
        //подготовим данные:
        Map<String, Object> newname = Map.of(
                "name", invalidName
        );
        //шлем запрос
        Response response =given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .body(newname)
                .when()
                .put("http://127.0.0.1:5000/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST).extract().response();
        String actualErrorMessage = response.asString().trim();
        assertEquals(actualErrorMessage, errorMessage);


        //Проверка: имя в поле name НЕ изменилось
        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.is(userId))
                .body("name", Matchers.nullValue());

        //Удаляем данные Админ получает id всех юзеров, удаляет их а потом проверяет удалились ли они
        List<Integer> ids = given()
                .header("Authorization", authAdminToken)
                .when()
                .get("http://127.0.0.1:5000/api/v1/admin/users")
                .then()
                .statusCode(200)
                .body("$", notNullValue())
                .body("id", everyItem(notNullValue()))
                .extract()
                .jsonPath().getList("id", Integer.class);
        for (Integer id : ids) {
            given()
                    .header("Authorization", authAdminToken)
                    .when()
                    .delete("http://127.0.0.1:5000/api/v1/admin/users/{id}", id)
                    .then()
                    .statusCode(HttpStatus.SC_OK);
        }
            //проверяем что все реально удалилось, вызываем опять метод get all users
            given()
                    .header("Authorization", authAdminToken)
                    .when()
                    .get("http://127.0.0.1:5000/api/v1/admin/users")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("$", empty())
                    .body("$", hasSize(0));


        }

    }

