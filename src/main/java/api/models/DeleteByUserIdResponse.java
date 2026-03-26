package api.models;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class DeleteByUserIdResponse extends BaseModel {
    private String successMessage;



}
