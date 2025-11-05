package models;

public class TransferMoney extends BaseModel {
    /*
    {
  "senderAccountId": 1,
  "receiverAccountId": 2,
  "amount": 250.75
}
     */

    private int senderAccountId;
    private int receiverAccountId;
    private float amount;

    public TransferMoney(int senderAccountId, int receiverAccountId, float amount) {
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;

    }
    public TransferMoney(){}

    public TransferMoney(Builder builder){
        this.senderAccountId = builder.senderAccountId;
        this.receiverAccountId = builder.receiverAccountId;
        this.amount = builder.amount;
    }

    public static class Builder {

        private int senderAccountId;
        private int receiverAccountId;
        private float amount;


        public Builder senderAccountId(int senderAccountId) {
            this.senderAccountId = senderAccountId;
            return this;
        }

        public Builder receiverAccountId(int receiverAccountId) {
            this.receiverAccountId = receiverAccountId;
            return this;
        }

        public Builder amount(float amount) {
            this.amount = amount;
            return this;
        }

        // Этот метод создаёт финальный объект
        public TransferMoney build() {
            return new TransferMoney(senderAccountId, receiverAccountId, amount);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getSenderAccountId() {
        return senderAccountId;
    }

    public int getReceiverAccountId() {
        return receiverAccountId;
    }

    public float getAmount() {
        return amount;
    }
}
