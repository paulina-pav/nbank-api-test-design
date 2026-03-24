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
public class AccountDao extends BaseDaoModel {
    private Long id;
    @ColumnName("account_number")
    private String accountNumber;
    private Double balance;
    @ColumnName("customer_id")
    private Long customerId;
}
