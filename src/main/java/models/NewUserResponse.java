package models;

import java.util.List;

public class NewUserResponse {
    private Integer id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<String> accounts;

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public List<String> getAccounts() {
        return accounts;
    }
    public NewUserResponse() {}


    public NewUserResponse(Integer id, String username, String password, String name, String role, List<String> accounts) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.accounts = accounts;
    }


    public static class Builder{
        private Integer id;
        private String username;
        private String password;
        private String name;
        private String role;
        private List<String> accounts;


        public Builder setId(Integer id) {
            this.id = id;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setRole(String role) {
            this.role = role;
            return this;
        }

        public Builder setAccounts(List<String> accounts) {
            this.accounts = accounts;
            return this;
        }
        public NewUserResponse build() {
            return new NewUserResponse(id, username, password, name, role, accounts);
        }
    }
    public static Builder builder() {
        return new Builder();
    }

}
