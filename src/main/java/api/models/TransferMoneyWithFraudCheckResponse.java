package api.models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class TransferMoneyWithFraudCheckResponse extends BaseModel {

    private String message;
    private boolean requiresVerification;
    private Long receiverAccountId;
    private Double fraudRiskScore;
    private boolean requiresManualReview;
    private Long transactionId;
    private String fraudReason;
    private Long senderAccountId;
    private String status;
    private Double amount;

}
