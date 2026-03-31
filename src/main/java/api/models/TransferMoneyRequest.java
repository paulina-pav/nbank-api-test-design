package api.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TransferMoneyRequest extends BaseModel {

    private Long senderAccountId;
    private Long receiverAccountId;
    private Double amount;

}
