package api.models;


import api.generators.GeneratingRule;
import lombok.*;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class NewUserRequest extends BaseModel {

    @GeneratingRule(regex = "^[A-Za-z0-9]{3,15}$")
    private String username;

    @GeneratingRule(regex = "^[A-Z]{3}[a-z]{4}[0-9]{3}[$%&]{2}$")
    private String password;

    @GeneratingRule(regex = "^USER$")
    private String role;


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NewUserRequest that = (NewUserRequest) o;
        return Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, role);
    }
}
