package Requests;

import Requests.methods.DeleteById;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.DeleteByUserId;

import static io.restassured.RestAssured.given;

public class DeleteUserByIdRequester extends Requests<DeleteByUserId> implements DeleteById<DeleteByUserId> {
    public DeleteUserByIdRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }


    @Override
    public ValidatableResponse delete(DeleteByUserId model) {
        return given()
                .spec(requestSpecification)
                .delete("/api/v1/admin/users/{id}", model.getId())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
