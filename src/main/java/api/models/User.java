package api.models;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter

public class User extends BaseModel{
    Integer id;
    String username;
    String password;
    String name;
    String role;

    List<Account> accounts;
}
