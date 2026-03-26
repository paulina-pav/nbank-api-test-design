package api.generators;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MaxSumsForDepositAndTransactions {
    DEPOSIT(5000.0),
    TRANSACTION(10000.0);

    private final Double max;

}
