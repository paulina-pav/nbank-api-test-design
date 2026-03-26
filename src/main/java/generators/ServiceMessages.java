package generators;

public enum ServiceMessages {
    SUCCESSFUL_TRANSFER("Transfer successful"),
    PROFILE_UPDATED_SUCCESSFULLY("Profile updated successfully");


    private final String message ;
    ServiceMessages(String message) { this.message = message; }
    public String getMessage() {
        return message;
    }
}
