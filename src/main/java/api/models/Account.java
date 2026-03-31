package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class Account extends BaseModel{
    Long id;
    String accountNumber;
    Double balance;
    String role;
    List<Transaction> transactions;
}
