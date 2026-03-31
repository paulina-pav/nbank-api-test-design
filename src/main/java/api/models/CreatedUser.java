package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CreatedUser {
    private NewUserRequest request;
    private NewUserResponse response;
}
