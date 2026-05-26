package api.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetAccountTransactionsResponse extends BaseModel {
    List<Transaction> transactionList;
    Long id;
    Double amount;
    String type;
    String timestamp;
    Long relatedAccountId;

}
