package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MockFraudCheckResponse extends BaseModel{
    private String status;
    private String decision;
    private Double riskScore;
    private String reason;
    private Boolean requiresManualReview;
    private Boolean additionalVerificationRequired;
}
