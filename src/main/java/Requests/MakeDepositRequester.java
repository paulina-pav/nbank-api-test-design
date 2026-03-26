package Requests;

import Requests.methods.Post;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.MakeDeposit;

import static io.restassured.RestAssured.given;

public class MakeDepositRequester extends Requests<MakeDeposit> implements Post<MakeDeposit> {


    public MakeDepositRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(MakeDeposit model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpecification);

    }
}
