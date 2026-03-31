package api.generators;

public enum ErrorMessage {
    NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY("Name must contain two words with letters only"),
    UNAUTHORIZED_ACCESS_TO_ACCOUNT("Unauthorized access to account"),
    TRANSFER_AMOUNT_MUST_BE_AT_LEAST_001("Transfer amount must be at least 0.01"),
    TRANSFER_AMOUNT_CANNOT_EXCEED_10000("Transfer amount cannot exceed 10000"),
    INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNT("Invalid transfer: insufficient funds or invalid accounts"),
    DEPOSIT_MUST_BE_AT_LEAST_001("Deposit amount must be at least 0.01"),
    DEPOSIT_AMOUNT_CANNOT_EXCEED_5000("Deposit amount cannot exceed 5000"),
    INVALID_ACCOUNT_OR_AMOUNT("Invalid account or amount"),
    DEPOSIT_AMOUNT_EXCEEDS_THE_5000_LIMIT("Deposit amount exceeds the 5000 limit");


    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
