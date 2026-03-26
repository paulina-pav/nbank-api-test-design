package api.models;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Transaction extends BaseModel{
    Long id;
    Double amount;
    String type;
    String timestamp;
    Long relatedAccountId;
}
