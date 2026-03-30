package db.models;

import db.mappers.ColumnName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDao extends BaseDaoModel {
    private Long id;
    private Double amount;
    private String type;
    @ColumnName("account_id")
    private Long accountId;
    @ColumnName("related_account_id")
    private Long relatedAccountId;

}
