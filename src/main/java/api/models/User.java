package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class User extends BaseModel{
    Integer id;
    String username;
    String password;
    String name;
    String role;

    List<Account> accounts;
}
