package Requests;

import Requests.methods.Get;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.GetCustomerProfile;

import static io.restassured.RestAssured.given;

public class GetCustomerProfileRequester extends  Requests<GetCustomerProfile> implements Get<GetCustomerProfile> {


    public GetCustomerProfileRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse get() {
        return given()
                .spec(requestSpecification)
                .get("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
