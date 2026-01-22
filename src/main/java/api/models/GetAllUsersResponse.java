package api.models;

import lombok.*;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter

public class GetAllUsersResponse extends BaseModel {
    List<User> users;
}
