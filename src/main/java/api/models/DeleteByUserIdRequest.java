package api.models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class DeleteByUserIdRequest extends BaseModel{
    private Integer id;

}
