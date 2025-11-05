package models;

import java.util.List;

public class GetCustomerAccountResponse extends BaseModel {
    private Integer id;
    private String accountNumber;
    private float balance;
    private List<Transaction> transactions; // <- имя совпадает с JSON

    public Integer getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public float getBalance() { return balance; }
    public List<Transaction> getTransactions() { return transactions; }

    public static class Transaction {
        private Integer id;               // <- не transactionId, а id
        private float amount;
        private String type;
        private String timestamp;
        private Integer relatedAccountId;

        public Integer getId() { return id; }
        public float getAmount() { return amount; }
        public String getType() { return type; }
        public String getTimestamp() { return timestamp; }
        public Integer getRelatedAccountId() { return relatedAccountId; }
    }
}
