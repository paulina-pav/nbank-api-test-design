package models;

public class GetAccountTransactions extends BaseModel{
    private final Integer id;

    public GetAccountTransactions(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    // Можно добавить билдер, если хочешь единообразия
    public static class Builder {
        private Integer id;
        public Builder setId(Integer id) {
            this.id = id;
            return this;
        }
        public GetAccountTransactions build() {
            return new GetAccountTransactions(id);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
