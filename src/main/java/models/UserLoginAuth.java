package models;

public class UserLoginAuth extends BaseModel {
    private String username;
    private String password;


    public UserLoginAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Вот тут начинается самое интересное — создаём внутренний Builder-класс
    public static class Builder {
        private String username;
        private String password;


        public UserLoginAuth.Builder username(String username) {
            this.username = username;
            return this;
        }

        public UserLoginAuth.Builder password(String password) {
            this.password = password;
            return this;
        }



        // Этот метод создаёт финальный объект
        public UserLoginAuth build() {
            return new UserLoginAuth(username, password);
        }
    }

    // Статический метод для инициализации билдера
    public static UserLoginAuth.Builder builder() {
        return new UserLoginAuth.Builder();
    }
}
