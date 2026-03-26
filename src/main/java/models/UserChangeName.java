package models;

public class UserChangeName extends BaseModel{
    private String name;

    public UserChangeName(String name) {
        this.name = name;
    }

    public UserChangeName(){}

    public String getName() {
        return name;
    }

    public static class Builder{
        private String name;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public UserChangeName build(){
            return new UserChangeName(name);
        }
    }

    public static Builder builder(){
        return new Builder();
    }

}
