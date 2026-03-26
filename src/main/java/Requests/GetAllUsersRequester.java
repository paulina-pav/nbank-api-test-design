package Requests;

import Requests.methods.Get;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.GetAllUsers;

import static io.restassured.RestAssured.given;

public class GetAllUsersRequester extends Requests<GetAllUsers> implements Get<GetAllUsers> {
    public GetAllUsersRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse get() {
        return given()
                .spec(requestSpecification)
                .get("/api/v1/admin/users")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
