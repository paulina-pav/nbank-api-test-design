package models;

public class DeleteByUserIdResponse {
    private String successMessage;

    public DeleteByUserIdResponse(String successMessage) {
        this.successMessage = successMessage;
    }
    public DeleteByUserIdResponse(){}

    public String getSuccessMessage() {
        return successMessage;
    }
}
