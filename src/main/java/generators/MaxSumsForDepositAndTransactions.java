package generators;

public enum MaxSumsForDepositAndTransactions {
    DEPOSIT(5000.0f),
    TRANSACTION(10000.0f);

    private final float max;
    MaxSumsForDepositAndTransactions(float max) { this.max = max; }
    public float getValue() { return max; }
}
