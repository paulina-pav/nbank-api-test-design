package models;

import java.util.List;

public class UserChangeNameResponse {
    private Customer customer;
    private String message;

    public Customer getCustomer() {
        return customer;
    }

    public String getMessage() {
        return message;
    }

    // внутренний класс или отдельный файл
    public static class Customer {
        private Integer id;
        private String username;
        private String password;
        private String name;
        private String role;
        private List<Object> accounts; // если объекты аккаунтов не нужны пока, можно оставить Object

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

        public List<Object> getAccounts() {
            return accounts;
        }
    }
}
