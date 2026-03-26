package negative.deposit;

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

public class UserMakesInvalidDeposit {
    /*

### Тест-кейс 1: Авторизованный юзер делает депозит -1 на свой аккаунт на свой аккаунт
### Тест-кейс 2: Авторизованный юзер делает депозит 0 на свой аккаунт
### Тест-кейс 3: Авторизованный юзер делает депозит 5001 на свой аккаунт

ГОТОВО

     */
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    public static Stream<Arguments> invalidSum(){
        return Stream.of(
                Arguments.of(-1, "Deposit amount must be at least 0.01"), //400
                Arguments.of(0, "Deposit amount must be at least 0.01"), //400
                Arguments.of(5001, "Deposit amount cannot exceed 5000")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidSum")
    //Тест-кейс 1: Авторизованный юзер делает депозит -1 на свой аккаунт на свой аккаунт
    public void userMakesInvalidDeposit(int invalidAmount, String expectedMessage){
        //Предусловие, шаг 1: админ получает токен
        String authAdminToken = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "admin",
                          "password": "admin"
                        }
                        """)
                .post("http://127.0.0.1:5000/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
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
                .statusCode(HttpStatus.SC_CREATED);
        //Предусловие, шаг 3. Проверим, что юзер создался:
        given()
                .header("Authorization", authAdminToken)
                .get("http://127.0.0.1:5000/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        //подготовим данные  для авторизации
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
                .extract()
                .header("Authorization");

        //Предусловие, шаг 5: юзер создает счет и запишем его в переменную
        int userAccountId =  given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .when()
                .post("http://127.0.0.1:5000/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");
        //Проверка, что счет создался
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", hasItem(userAccountId));
        //Действие: юзер делает депозит на счет:

        //Подготовим тело запроса:
        Map<String, Object> deposit = Map.of(
                "id", userAccountId,
                "balance", invalidAmount  //сумму поставить сюда!

        );
        //делаем запрос с неверными данными и записываем ответ. Затем рассматриваем его как строку и валидируем через ассерт
        Response response = given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .body(deposit)
                .post("http://127.0.0.1:5000/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                        .extract()
                                .response();
        String actualBody = response.asString().trim();
        assertEquals(expectedMessage, actualBody);


        //проверяем сумму на балансе
        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].id", is(userAccountId))
                .body("[0].balance", Matchers.is(0.0F));
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
        for(Integer id: ids) {
            given()
                    .header("Authorization", authAdminToken)
                    .when()
                    .delete("http://127.0.0.1:5000/api/v1/admin/users/{id}", id)
                    .then()
                    .statusCode(HttpStatus.SC_OK);
        }
            //проверяем что все реально удалилось, вызываем опять метод get all users
            given()
                    .header("Authorization",authAdminToken)
                    .when()
                    .get("http://127.0.0.1:5000/api/v1/admin/users")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("$", empty())
                    .body("$", hasSize(0));
    }




}

