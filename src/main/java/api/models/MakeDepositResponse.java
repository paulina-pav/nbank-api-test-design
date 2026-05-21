package api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MakeDepositResponse extends BaseModel {

        private Long id;
        private String accountNumber;
        private Double balance;
        private Double depositAmount;
        private Long transactionId;

        /*
        {
    "id": 14,
    "accountNumber": "ACC7A30461B",
    "balance": 5000.00,
    "depositAmount": 5000.0,
    "transactionId": 8
}
         */
}

