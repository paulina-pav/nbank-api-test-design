package api.models;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class TransferMoneyRequest extends BaseModel {

    private Long senderAccountId;
    private Long receiverAccountId;
    private Double amount;

}
