package Requests;

import Requests.methods.Post;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.MakeDeposit;
import models.TransferMoney;

import static io.restassured.RestAssured.given;

public class TransferMoneyRequester extends Requests<TransferMoney> implements Post<TransferMoney> {


    public TransferMoneyRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(TransferMoney model){
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }


    /*
    пример тела запроса:
    {
  "senderAccountId": 1,
  "receiverAccountId": 2,
  "amount": 250.75
}
     */



}
