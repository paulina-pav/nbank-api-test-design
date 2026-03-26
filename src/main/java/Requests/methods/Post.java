package Requests.methods;

import io.restassured.response.ValidatableResponse;
import models.BaseModel;

public interface Post <T extends BaseModel>{
    ValidatableResponse post(T model);
}
