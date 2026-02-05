package api.requests.steps.result;

import api.models.NewUserRequest;
import api.models.NewUserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatedUser {
    private NewUserRequest request;   // test data
    private NewUserResponse response; // API result
}
