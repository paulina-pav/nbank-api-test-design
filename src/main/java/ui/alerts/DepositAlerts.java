package ui.alerts;

import lombok.Getter;

@Getter
public enum DepositAlerts {
    SELECT_ACCOUNT("❌ Please select an account."),
    DEPOSIT_LESS_THAN_5001("❌ Please deposit less or equal to 5000$."),
    ENTER_VALID_AMOUNT("❌ Please enter a valid amount.");


    private final String message;

    DepositAlerts(String message) {
        this.message = message;
    }
}
