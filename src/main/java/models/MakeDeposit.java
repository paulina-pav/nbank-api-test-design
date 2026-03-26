package models;

public class MakeDeposit extends BaseModel {

/*
{
  "id": 1,
  "balance": 100.5
}
 */

    private float balance;
    private Integer id;



    public MakeDeposit() {}

    public MakeDeposit(Integer id, float balance) {
        this.balance = balance;
        this.id = id;
    }

    public MakeDeposit(Builder builder){
        this.balance = builder.balance;
        this.id = builder.id;
    }

    public static class Builder {
        private Integer id;
        private float balance;

        public Builder setId(Integer id) {
            this.id = id;
            return this;
        }

        public Builder setBalance(float balance) {
            this.balance = balance;
            return this;
        }

        // Этот метод создаёт финальный объект
        public MakeDeposit build() {
            return new MakeDeposit(id, balance);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Integer getId() {
        return id;
    }

    public float getBalance() {
        return balance;
    }
}
