package api.models;

import lombok.*;

import java.util.List;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class GetCustomerAccountResponse extends BaseModel {
    private Long id;
    private String accountNumber;
    private Double balance;
    private List<Transaction> transactions;

}
