package api.models;


import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class GetAccountTransactionsResponse extends BaseModel {
    List<Transaction> transactionList;
    Long id;
    Double amount;
    String type;
    String timestamp;
    Long relatedAccountId;

}
