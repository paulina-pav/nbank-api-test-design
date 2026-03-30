package api.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetAccountTransactionsResponse extends BaseModel {
    List<Transaction> transactionList;
    Long id;
    Double amount;
    String type;
    String timestamp;
    Long relatedAccountId;

}
