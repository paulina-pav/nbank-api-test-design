package Requests.methods;

import io.restassured.response.ValidatableResponse;
import models.BaseModel;

public interface DeleteById<T extends BaseModel> {
    ValidatableResponse delete(T model);
}
