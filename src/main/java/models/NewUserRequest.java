package models;


import lombok.*;


public class NewUserRequest extends BaseModel {

   private String username;
    private String password;
    private String role;

    public NewUserRequest() {}

    public NewUserRequest(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public NewUserRequest(Builder builder){
        this.username = builder.username;
        this.password = builder.password;
        this.role = builder.role;
    }

    public static class Builder {
        private String username;
        private String password;
        private String role;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        // Этот метод создаёт финальный объект
        public NewUserRequest build() {
            return new NewUserRequest(username, password, role);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

}
