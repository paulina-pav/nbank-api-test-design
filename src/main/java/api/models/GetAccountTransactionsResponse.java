package api.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
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
