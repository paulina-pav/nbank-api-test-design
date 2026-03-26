package positive;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserTransferMoneyTest {
    /*
Тест-кейсы этого файла:
1. Юзер успешно переводит деньги на существуюющий счет другого юзера
2. Юзер успешно переводит деньги с одного своего счета на другой

     */
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    //Тест-кейс 1. Юзер успешно переводит деньги на существуюющий счет другого юзера
    @Test
    public void userTransfersMoneyToUser() {

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
                "receiverAccountId", userAccountId2,
                "amount", 5000.0F

        );
        given()
                .contentType(ContentType.JSON)
                .accept("*/*")
                .header("Authorization", "Basic a2F0ZTE5OTY6MTUxMDE5OTZLYXRlIQ==")
                .when()
                .body(transferringMoneyBody)
                .post("http://127.0.0.1:5000/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Transfer successful"))
                .body("amount", equalTo(5000.0F));

        //Проверка: у юзера 1 уменьшился баланс на сумму из предыдущего шага и появилась транзакция transfer_out с нужной суммой

        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].id", is(userAccountId))
                .body("[0].balance", Matchers.is(0.0F));
        //Проверка: у юзера 2 увеличился баланс на сумму из предыдущего шага и появилась транзакция transfer_out с нужной суммой
        given()
                .header("Authorization", userAuthToken2)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("$", hasSize(greaterThan(0)))
                .body("[0].id", is(userAccountId2))
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



    //Тест-кейс 2: Юзер успешно переводит деньги с одного своего счета на другой
    @Test
    public void userTransfersMoneyBetweenItsAccounts(){

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

        //нужно создать 2 счета одному юзеру. Создадим массив, куда будем записывать счета и с помощью цикла создадим счет дважды
        ArrayList<Integer> accounts = new ArrayList<>();

        for(int i = 0; i <= 1; i++){
            //Предусловие, шаг 5: юзер создает счет и запишем его в переменную
            Integer userAccountId = given()
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
            //добавим счет в массив
            accounts.add(userAccountId);
        }


        //Юзер делает депозит на свой счет:
        //Подготовим тело запроса:
        Map<String, Object> deposit = Map.of(
                "id", accounts.getFirst(),
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
                .body("id", Matchers.is(accounts.getFirst()))
                .body("balance", Matchers.notNullValue())
                .body("transactions", Matchers.not(empty()))
                .body("transactions.type", hasItem("DEPOSIT"))
                .body("transactions.relatedAccountId", hasItem(accounts.getFirst()))
                .body("transactions.amount", Matchers.notNullValue());
        //проверяем сумму на балансе
        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.balance == 0 }", notNullValue())
                .body("findAll { it.balance == 5000 }.size()", greaterThan(0));



        //Действие: юзер 1 делает со своего счета перевод на свой другой счет
        //готовим тело запроса:
        Map<String, Object> transferringMoneyBody = Map.of(
                "senderAccountId", (accounts.get(0)),
                "receiverAccountId", (accounts.get(1)),
                "amount", 5000.0F

        );

        given()
                .contentType(ContentType.JSON)
                // .accept("*//*")
                .header("Authorization", "Basic a2F0ZTE5OTY6MTUxMDE5OTZLYXRlIQ==")
                .when()
                .body(transferringMoneyBody)
                .post("http://127.0.0.1:5000/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", equalTo("Transfer successful"))
                .body("amount", equalTo(5000.0F));

        //Проверим транзакции для счетов


        given()
                .header("Authorization", userAuthToken)
                .when()
                .get("http://127.0.0.1:5000/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThanOrEqualTo(2))
                // есть аккаунт с балансом 0
                .body("find { it.balance == 0 }", notNullValue())
                // у него есть транзакция TRANSFER_OUT
                .body("find { it.balance == 0 }.transactions.type", hasItem("TRANSFER_OUT"))

                // есть аккаунт с балансом 5000
                .body("find { it.balance == 5000 }", notNullValue())
                // у него есть транзакция TRANSFER_IN
                .body("find { it.balance == 5000 }.transactions.type", hasItem("TRANSFER_IN"));


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




