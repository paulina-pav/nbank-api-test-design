package negative.deposit;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;



/*
Тест-кейсы, которые тут есть:

### Тест-кейс 4: Авторизованный юзер делает депозит на чужой аккаунт
### Тест-кейс 5: Авторизованный юзер делает депозит на несуществующий аккаунт

 */


//Тест-кейс 5: Авторизованный юзер делает депозит на несуществующий аккаунт
public class UserMakesDepositUnknownAndIllegalAccount {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    public static Stream<Arguments> invalidAccounts(){
        return Stream.of(
                Arguments.of(100500, "Unauthorized access to account") //403 //несуществующий аккаунт
        );
    }
    @ParameterizedTest
    @MethodSource("invalidAccounts")
    //Тест-кейс 1: Авторизованный юзер делает депозит -1 на свой аккаунт на свой аккаунт
    public void userMakesInvalidDeposit(int invalidAccount, String expectedMessage){
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
                "id", invalidAccount,
                "balance", 5000

        );
        //делаем запрос с неверными данными и записываем ответ. Затем рассматриваем его как строку и валидируем через ассерт
        Response response = given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .body(deposit)
                .post("http://127.0.0.1:5000/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
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



    //Тест-кейс 4: Авторизованный юзер делает депозит на чужой аккаунт
    @Test
    public void userMakesDepositOnTheAccOtherUser(){
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
                .statusCode(HttpStatus.SC_CREATED);
               /* .body("username", Matchers.equalTo("kate1996"))
                .body("password", Matchers.not(Matchers.equalTo("15101996Kate!")))
                .body("role", Matchers.equalTo("USER"));*/

        //Предусловие, шаг 3. Проверим, что юзер создался:
        given()
                .header("Authorization", authAdminToken)
                .get("http://127.0.0.1:5000/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        //Создаем юзера 2
        Map<String, Object> bodyToCreateUser2 = Map.of(
                "username", "polina1996",
                "password", "15101996Polina!",
                "role", "USER"
        );
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", authAdminToken)
                .body(bodyToCreateUser2)
                .post("http://127.0.0.1:5000/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
                /*.body("username", Matchers.equalTo("polina1996"))
                .body("password", Matchers.not(Matchers.equalTo("15101996Polina!")))
                .body("role", Matchers.equalTo("USER"));*/

        //Проверим, что юзер2 создался:
        given()
                .header("Authorization", authAdminToken)
                .get("http://127.0.0.1:5000/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        //Готовим данные для авторизации Юзера2
        Map<String, Object> userAuthData2 = Map.of(
                "username", "polina1996",
                "password", "15101996Polina!"

        );
        //юзер 2 авторизуется - подтягиваем токен
        String userAuthToken2 = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(userAuthData2)
                .post("http://127.0.0.1:5000/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .header("Authorization", Matchers.notNullValue())
                .extract()
                .header("Authorization");
        //юзер 2 создает счет, записываем его в переменную
        int userAccountId2 = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken2)
                .when()
                .post("http://127.0.0.1:5000/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("id");
        //Проверка, что счет у юзера 2 создался
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken2)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", hasItem(userAccountId2));

        //Переходим к юзеру 1. Подготовим данные для авторизации юзера 1
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
        //cейчас юзер 2 имеет счет, а юзер 1 нет.
        //создадим запрос от имени юзера 1, но вставим счет юзера 2.

        //Действие: юзер делает депозит на счет:
        //Подготовим тело запроса: счет юзера 2
        Map<String, Object> deposit = Map.of(
                "id", userAccountId2,
                "balance", 5000

        );
        Response response = given()
                .header("Authorization", userAuthToken) //авторизуемся как юзер 1
                .contentType(ContentType.JSON)
                .body(deposit)
                .post("http://127.0.0.1:5000/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .extract()
                .response();
        String actualBody = response.asString().trim();
        assertEquals("Unauthorized access to account", actualBody);
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
