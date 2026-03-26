package models;

public class UserLoginAuthResponse {
    private String username;
    private String role;

    public UserLoginAuthResponse(String username, String role) {
        this.username = username;
        this.role = role;
    }
    public UserLoginAuthResponse(){}

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }


    public UserLoginAuthResponse(Builder builder){
        this.username = builder.username;
        this.role = builder.role;
    }

    public static class Builder{
        private String username;
        private String role;

        public Builder setRole(String role) {
            this.role = role;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }


        public UserLoginAuthResponse build() {
            return new UserLoginAuthResponse(username, role);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
