package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckFraudDetectionStatusResponse extends BaseModel {
    private String status;
    private Long transactionId;
    private String note;

    /*
    {
  "status": "NO_FRAUD_CHECK_REQUIRED",
  "transactionId": 68,
  "note": "This transaction does not require fraud checking."
}
     */
}
