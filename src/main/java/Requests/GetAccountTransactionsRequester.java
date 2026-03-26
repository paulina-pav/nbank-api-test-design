package Requests;

import Requests.methods.GetByAccountId;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.GetAccountTransactions;

import static io.restassured.RestAssured.given;

public class GetAccountTransactionsRequester extends Requests<GetAccountTransactions> implements GetByAccountId<GetAccountTransactions> {

    public GetAccountTransactionsRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse get(GetAccountTransactions model) {
        return given()
                .spec(requestSpecification)
                .get("/api/v1/accounts/{id}/transactions", model.getId())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
