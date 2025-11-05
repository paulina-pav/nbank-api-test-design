package models;

public class GetAccountTransactionsResponse extends BaseModel {
    /*
    [
  {
    "id": 1,
    "amount": 100.5,
    "type": "DEPOSIT",
    "timestamp": "Wed Oct 29 12:53:21 UTC 2025",
    "relatedAccountId": 1
  },
  {
    "id": 4,
    "amount": 100.5,
    "type": "DEPOSIT",
    "timestamp": "Wed Oct 29 13:11:29 UTC 2025",
    "relatedAccountId": 1
  }
]
     */

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
