package db.requester;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestSkeleton {

    private Map<String, Object> values;
    private RequestType requestType;
    private String table;
    private List<Condition> conditions;

    public <T> T extractAs(Class<T> clazz) {
        return new Executor().extractAs(this, clazz);
    }

    public int execute() {
        return new Executor().execute(this);
    }


    public static DBRequestBuilder builder() {
        return new DBRequestBuilder();
    }

    public static class DBRequestBuilder {
        private Map<String, Object> values = new LinkedHashMap<>();
        private RequestType requestType;
        private String table;
        private List<Condition> conditions = new ArrayList<>();

        public DBRequestBuilder requestType(RequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public DBRequestBuilder where(Condition condition) {
            this.conditions.add(condition);
            return this;
        }

        public DBRequestBuilder table(String table) {
            this.table = table;
            return this;
        }

        public RequestSkeleton build() {
            return new RequestSkeleton(values, requestType, table, conditions);
        }

        public <T> T extractAs(Class<T> clazz) {
            RequestSkeleton request = build();
            return request.extractAs(clazz);
        }

        public int execute() {
            RequestSkeleton request = build();
            return request.execute();
        }

        public DBRequestBuilder value(String column, Object value) {
            this.values.put(column, value);
            return this;
        }
    }


}
