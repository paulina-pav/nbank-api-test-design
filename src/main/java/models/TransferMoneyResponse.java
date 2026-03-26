package models;

public class TransferMoneyResponse {
    /*
    {
    "senderAccountId": 1,
    "receiverAccountId": 2,
    "amount": 10.0,
    "message": "Transfer successful"
}
     */

    private int senderAccountId;
    private int receiverAccountId;
    private float amount;
    private String message;

    public int getSenderAccountId() {
        return senderAccountId;
    }

    public int getReceiverAccountId() {
        return receiverAccountId;
    }

    public float getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }
}
