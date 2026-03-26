package Requests.methods;

import io.restassured.response.ValidatableResponse;
import models.BaseModel;

public interface Get <T extends BaseModel> {
    ValidatableResponse get();
}
