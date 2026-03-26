package Requests.methods;

import io.restassured.response.ValidatableResponse;
import models.BaseModel;

public interface Put <T extends BaseModel>{
    ValidatableResponse put(T model);
}
