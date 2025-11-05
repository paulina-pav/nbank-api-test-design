package Requests;

import Requests.methods.Post;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import models.NewUserRequest;

import static io.restassured.RestAssured.given;

public class CreateNewUserRequester extends Requests<NewUserRequest> implements Post<NewUserRequest> {
    public CreateNewUserRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(NewUserRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/admin/users")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
