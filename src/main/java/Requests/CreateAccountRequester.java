package Requests;

import Requests.methods.Post;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.CreateAnAccount;

import static io.restassured.RestAssured.given;

public class CreateAccountRequester extends Requests<CreateAnAccount> implements Post<CreateAnAccount> {


    public CreateAccountRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(CreateAnAccount model) {
        return given()
                .spec(requestSpecification)
                .post("/api/v1/accounts")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
