package models;

public class GetCustomerProfile extends BaseModel {


    public GetCustomerProfile() {}

    public GetCustomerProfile(Builder builder){

    }

    public static class Builder {

        // Этот метод создаёт финальный объект
        public GetCustomerProfile build() {
            return new GetCustomerProfile();
        }
    }

    public static GetCustomerProfile builder() {
        return new GetCustomerProfile();
    }

}
