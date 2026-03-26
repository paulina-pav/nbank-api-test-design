package Requests;

import Requests.methods.Post;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import models.UserLoginAuth;

import static io.restassured.RestAssured.given;

public class UserLoginAuthRequester extends Requests<UserLoginAuth> implements Post<UserLoginAuth> {

    public UserLoginAuthRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(UserLoginAuth model) {
        return  given()
                .spec(requestSpecification)
                .body(model)
                .post("api/v1/auth/login")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
