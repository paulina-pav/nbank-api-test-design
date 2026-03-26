package negative.transfer;

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

public class UserTransferMoneyUnknownAcc {

    /*
    Тест-кейс этого файла: Юзер переводит деньги на несуществующий счет
     */

    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }



    public static Stream<Arguments> invalidSumToTransfer(){
        return Stream.of(

                Arguments.of(100500, "Invalid transfer: insufficient funds or invalid accounts")
        );
    }


    @ParameterizedTest
    @MethodSource("invalidSumToTransfer")
    public void userTransfersMoneyToUser(int invalidAcc, String errorMessage) {

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

        //Подготовим данные для авторизации юзера 1
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

        //Предусловие, шаг 5: юзер создает счет и запишем его в переменную
        int userAccountId = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .when()
                .post("http://127.0.0.1:5000/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("$", allOf(
                        hasKey("id"), hasKey("accountNumber"),
                        hasKey("balance"), hasKey("transactions")))
                .body("id", notNullValue())
                .body("accountNumber", notNullValue())
                .body("balance", is(0.0F))
                .body("transactions", empty())
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
        //Юзер делает депозит на свой счет:
        //Подготовим тело запроса:
        Map<String, Object> deposit = Map.of(
                "id", userAccountId,
                "balance", 5000

        );
        given()
                .header("Authorization", userAuthToken)
                .contentType(ContentType.JSON)
                .body(deposit)
                .post("http://127.0.0.1:5000/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.is(userAccountId))
                .body("balance", Matchers.notNullValue())
                .body("transactions", Matchers.not(empty()))
                .body("transactions.type", hasItem("DEPOSIT"))
                .body("transactions.relatedAccountId", hasItem(userAccountId))
                .body("transactions.amount", Matchers.notNullValue());
        //проверяем сумму на балансе
        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].id", is(userAccountId))
                .body("[0].balance", Matchers.is(5000.0F));
        //Действие: юзер 1 делает со своего счета перевод на счет юзера 2:
        //готовим тело запроса:
        Map<String, Object> transferringMoneyBody = Map.of(
                "senderAccountId", userAccountId,
                "receiverAccountId", invalidAcc,
                "amount", 5000.0F

        );
        Response response = given()
                .contentType(ContentType.JSON)
                .accept("*/*")
                .header("Authorization", "Basic a2F0ZTE5OTY6MTUxMDE5OTZLYXRlIQ==")
                .when()
                .body(transferringMoneyBody)
                .post("http://127.0.0.1:5000/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                        .extract()
                                .response();
        String actual = response.asString().trim();
        assertEquals(errorMessage, actual);


        //Проверка: у юзера 1 баланс такой же

        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].id", is(userAccountId))
                .body("[0].balance", Matchers.is(5000.0F));

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
    public static Stream<Arguments> tooBigSum(){
        return Stream.of(

                Arguments.of(10001.0F, "Transfer amount cannot exceed 10000")
        );
    }
    @ParameterizedTest @MethodSource("tooBigSum")

    public void userTransfersTooBigSum(double bigSum, String errorMessage) {

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
                .body("$", allOf(
                        hasKey("id"), hasKey("accountNumber"),
                        hasKey("balance"), hasKey("transactions")))
                .body("id", notNullValue())
                .body("accountNumber", notNullValue())
                .body("balance", is(0.0F))
                .body("transactions", empty())
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

        //Предусловие, шаг 5: юзер создает счет и запишем его в переменную
        int userAccountId = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", userAuthToken)
                .when()
                .post("http://127.0.0.1:5000/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("$", allOf(
                        hasKey("id"), hasKey("accountNumber"),
                        hasKey("balance"), hasKey("transactions")))
                .body("id", notNullValue())
                .body("accountNumber", notNullValue())
                .body("balance", is(0.0F))
                .body("transactions", empty())
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
        //Юзер делает депозит на свой счет:
        //Подготовим тело запроса:
        Map<String, Object> deposit = Map.of(
                "id", userAccountId,
                "balance", 5000.0F

        );

        //трижды кидаем 5000
        for (int i = 0; i <= 2; i++) {
            given()
                    .header("Authorization", userAuthToken)
                    .contentType(ContentType.JSON)
                    .body(deposit)
                    .post("http://127.0.0.1:5000/api/v1/accounts/deposit")
                    .then()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("id", Matchers.is(userAccountId))
                    .body("balance", Matchers.notNullValue())
                    .body("transactions", Matchers.not(empty()))
                    .body("transactions.type", hasItem("DEPOSIT"))
                    .body("transactions.relatedAccountId", hasItem(userAccountId))
                    .body("transactions.amount", Matchers.notNullValue());

        }

        //проверяем сумму на балансе
        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].id", is(userAccountId))
                .body("[0].balance", Matchers.is(15000.0F));

        //Действие: юзер 1 делает со своего счета перевод на счет юзера 2:
        // готовим тело запроса:
        Map<String, Object> transferringMoneyBody = Map.of(
                "senderAccountId", userAccountId,
                "receiverAccountId", userAccountId2,
                "amount", bigSum

        );
        Response response =  given()
                .contentType(ContentType.JSON)
                .accept("*/*")
                .header("Authorization", "Basic a2F0ZTE5OTY6MTUxMDE5OTZLYXRlIQ==")
                .when()
                .body(transferringMoneyBody)
                .post("http://127.0.0.1:5000/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract()
                .response();
        String actualResult = response.asString().trim();
        assertEquals(errorMessage, actualResult);

        //Проверка: у юзера1 такой же баланс

        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].id", is(userAccountId))
                .body("[0].balance", Matchers.is(15000.0F));
        //Проверка: у юзера 2 такой же баланс
        given()
                .header("Authorization", userAuthToken2)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].id", is(userAccountId2))
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
