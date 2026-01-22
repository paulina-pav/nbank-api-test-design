package api.models;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter

public class Account extends BaseModel{
    Long id;
    String accountNumber;
    Double balance;
    String role;
    List<Transaction> transactions;
}
