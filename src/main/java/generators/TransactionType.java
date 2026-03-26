package generators;

public enum TransactionType {
    TRANSFER_IN("TRANSFER_IN"),
    TRANSFER_OUT("TRANSFER_OUT"),
    DEPOSIT("DEPOSIT");


    private final String type ;

    TransactionType(String type) { this.type = type; }
    public String getMessage() {
        return type;
    }
}
