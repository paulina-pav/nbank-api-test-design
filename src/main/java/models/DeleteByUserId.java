package models;

public class DeleteByUserId extends BaseModel{
    private final Integer id;

    public DeleteByUserId(Integer id) {
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
        public DeleteByUserId build() {
            return new DeleteByUserId(id);
        }
    }

    public static Builder builder() {
        return new Builder();
    }


}
