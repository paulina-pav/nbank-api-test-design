package api.requests.skelethon.requesters;

import api.configs.Config;
import common.helpers.StepLogger;
import api.models.BaseModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndpointInterface;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface {

    private final static String API_VERSION = Config.getProperty("apiVersion");


    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint,
                         ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return StepLogger.log("POST запрос на " + endpoint.getUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .post(API_VERSION + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse get() {
        return StepLogger.log("GET запрос на " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .get(API_VERSION + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse put(BaseModel model) {
        return StepLogger.log("PUT запрос на " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .body(model)
                    .put(API_VERSION + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse delete(long id) {
        return StepLogger.log("DELETE запрос на " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .delete(API_VERSION + endpoint.getUrl() + id)
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse get(long id) {
        return StepLogger.log("GET запрос на " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .pathParams("id", id)
                    .get(API_VERSION + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public Object update(long id, BaseModel model) {
        return StepLogger.log("PUT запрос на " + endpoint.getUrl(), () -> {
            return null;
        });
    }
}
