package models;

import java.util.List;

public class MakeDepositResponse {
    /*
    {
  "id": 1,//номер счета
  "accountNumber": "ACC1",
  "balance": 100.5,
  "transactions": [
    {
      "id": 1, //номер транзакции
      "amount": 100.5,
      "type": "DEPOSIT",
      "timestamp": "Wed Oct 29 12:53:21 UTC 2025",
      "relatedAccountId": 1 //номер счета
    }
  ]
}
     */


        private Integer id;
        private String accountNumber;
        private float balance;
        private List<Transaction> transactions;

        // --- геттеры ---
        public Integer getId() {
            return id;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public float getBalance() {
            return balance;
        }

        public List<Transaction> getTransactions() {
            return transactions;
        }

        // --- вложенный класс ---
        public static class Transaction {
            private Integer id;
            private double amount;
            private String type;
            private String timestamp;
            private Integer relatedAccountId;

            public Integer getId() {
                return id;
            }

            public double getAmount() {
                return amount;
            }

            public String getType() {
                return type;
            }

            public String getTimestamp() {
                return timestamp;
            }

            public Integer getRelatedAccountId() {
                return relatedAccountId;
            }
        }
}

