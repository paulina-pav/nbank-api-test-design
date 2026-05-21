package api.generators;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GetTransactionWithFraudStatus {


    STATUS_NO_FRAUD_CHECK_REQUIRED("NO_FRAUD_CHECK_REQUIRED"),
    NOTE_DOES_NOT_REQUIRE("This transaction does not require fraud checking.");


    private final String status;
}
