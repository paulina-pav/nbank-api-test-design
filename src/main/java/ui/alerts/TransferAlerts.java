package ui.alerts;

import lombok.Getter;

@Getter
public enum TransferAlerts {
    FILL_ALL_FIELDS("❌ Please fill all fields and confirm."),
    TRANSFER_TO_THE_SAME_ACCOUNT("❌ You cannot transfer money to the same account."),
    NO_USER_WITH_THIS_ACCOUNT_NUMBER("❌ No user found with this account number"),
    INSUFFICIENT_FUNDS("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    RECIPIENT_NAME_NOT_MATCH("❌ The recipient name does not match the registered name.");



    private final String message;

    TransferAlerts(String message) {
        this.message = message;
    }
}
