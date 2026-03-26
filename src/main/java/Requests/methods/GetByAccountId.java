package Requests.methods;

import io.restassured.response.ValidatableResponse;
import models.BaseModel;

public interface GetByAccountId <T extends BaseModel>{
    ValidatableResponse get(T model);
}
