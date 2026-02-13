package ui.alerts;

import lombok.Getter;

@Getter
public enum TransferAgainAlerts {
    TRY_AGAIN("❌ Transfer failed: Please try again.");


    private final String message;

    TransferAgainAlerts(String message) {
        this.message = message;
    }
}
