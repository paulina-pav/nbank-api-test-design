package models;

import java.util.List;

public class CreateAnAccResponse {
    /*
    {
  "id": 1, //номер счета?
  "accountNumber": "ACC1",
  "balance": 0,
  "transactions": []
}
     */


    private Integer id;
    private String accountNumber;
    private float balance;
    private List<Object> transactions;


    public List<Object> getTransactions() {
        return transactions;
    }

    public float getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Integer getId() {
        return id;
    }
}
