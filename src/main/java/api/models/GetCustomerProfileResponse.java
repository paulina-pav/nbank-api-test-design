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
public class GetCustomerProfileResponse extends BaseModel {

    private Integer id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<Account> accounts;
}
