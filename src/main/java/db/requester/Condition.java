package db.requester;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Condition {
    private String column;
    private Object value;
    private String operator;

    public static Condition equalTo(String column, Object value) {
        return new Condition(column, value, "=");
    }

    public static Condition notEqualTo(String column, Object value) {
        return new Condition(column, value, "!=");
    }

    public static Condition like(String column, String value) {
        return new Condition(column, value, "LIKE");
    }

    public static Condition isNull(String column) {
        return new Condition(column, null, "IS NULL");
    }
}
