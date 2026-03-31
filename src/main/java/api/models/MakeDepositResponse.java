package api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MakeDepositResponse extends BaseModel {

        private Long id;
        private String accountNumber;
        private Double balance;
        private List<Transaction> transactions;

}

