package api.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatedUser {
    private NewUserRequest request;
    private NewUserResponse response;
}
