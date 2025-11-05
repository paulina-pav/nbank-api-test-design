package models;

public class CreateAnAccount extends BaseModel {

    public CreateAnAccount() {}

    /*public CreateAnAccount(Builder builder){

    }*/

    public static class Builder {

        // Этот метод создаёт финальный объект
        public CreateAnAccount build() {
            return new CreateAnAccount();
        }
    }

    public static CreateAnAccount builder() {
        return new CreateAnAccount();
    }
}
