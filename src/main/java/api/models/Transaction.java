package api.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction extends BaseModel{
    Long id;
    Double amount;
    String type;
    String timestamp;
    Long relatedAccountId;
}
