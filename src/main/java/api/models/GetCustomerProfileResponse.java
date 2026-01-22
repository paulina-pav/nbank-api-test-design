package api.models;

import lombok.*;

import java.util.List;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class GetCustomerProfileResponse extends BaseModel {

    private Integer id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<Account> accounts;
}
