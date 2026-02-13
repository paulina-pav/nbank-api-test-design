package ui.alerts;

import lombok.Getter;

@Getter
public enum ChangeNameAlerts {
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),

    ENTER_VALID_NAME("❌ Please enter a valid name."),
    NAME_CONTAINS_TWO_WORDS("Name must contain two words with letters only");

    private final String message;

    ChangeNameAlerts(String message) {
        this.message = message;
    }
}
