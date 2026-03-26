package Requests;

import Requests.methods.Put;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.UserChangeName;

import static io.restassured.RestAssured.given;

public class UserChangeNameRequester extends Requests<UserChangeName> implements Put<UserChangeName> {


    public UserChangeNameRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }


    @Override
    public ValidatableResponse put(UserChangeName model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .log().body()
                .when()
                .put("/api/v1/customer/profile")
                .then()
                .spec(responseSpecification);
    }
}
