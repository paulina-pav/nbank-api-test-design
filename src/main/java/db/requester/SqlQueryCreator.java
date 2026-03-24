package db.requester;

import java.util.List;

public class SqlQueryCreator {
    public String buildSQL(RequestSkeleton request) {
        return switch (request.getRequestType()) {
            case SELECT -> buildSelectSql(request);
            case DELETE -> buildDeleteSql(request);
            case INSERT -> buildInsertSql(request);
            case UPDATE -> buildUpdateSql(request);
            default -> throw new UnsupportedOperationException(
                    "Request type " + request.getRequestType() + " not implemented"
            );
        };
    }

    private String buildSelectSql(RequestSkeleton request) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(request.getTable());

        List<Condition> conditions = request.getConditions();
        if (conditions != null && !conditions.isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < conditions.size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }

                Condition condition = conditions.get(i);

                sql.append(condition.getColumn())
                        .append(" ")
                        .append(condition.getOperator());

                if (operatorRequiresValue(condition)) {
                    sql.append(" ?");
                }
            }
        }

        return sql.toString();
    }

    private String buildDeleteSql(RequestSkeleton request) {
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(request.getTable());

        List<Condition> conditions = request.getConditions();
        if (conditions != null && !conditions.isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < conditions.size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }

                Condition condition = conditions.get(i);

                sql.append(condition.getColumn())
                        .append(" ")
                        .append(condition.getOperator());

                if (operatorRequiresValue(condition)) {
                    sql.append(" ?");
                }
            }
        }

        return sql.toString();
    }

    private String buildInsertSql(RequestSkeleton request) {
        if (request.getValues() == null || request.getValues().isEmpty()) {
            throw new IllegalArgumentException("INSERT request must contain values");
        }

        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(request.getTable())
                .append(" (");

        StringBuilder placeholders = new StringBuilder();

        int index = 0;
        for (String column : request.getValues().keySet()) {
            if (index > 0) {
                sql.append(", ");
                placeholders.append(", ");
            }

            sql.append(column);
            placeholders.append("?");

            index++;
        }

        sql.append(") VALUES (")
                .append(placeholders)
                .append(")");

        return sql.toString();
    }

    private String buildUpdateSql(RequestSkeleton request) {
        if (request.getValues() == null || request.getValues().isEmpty()) {
            throw new IllegalArgumentException("UPDATE request must contain values");
        }

        StringBuilder sql = new StringBuilder("UPDATE ")
                .append(request.getTable())
                .append(" SET ");

        int valueIndex = 0;
        for (String column : request.getValues().keySet()) {
            if (valueIndex > 0) {
                sql.append(", ");
            }

            sql.append(column).append(" = ?");
            valueIndex++;
        }

        List<Condition> conditions = request.getConditions();
        if (conditions != null && !conditions.isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < conditions.size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }

                Condition condition = conditions.get(i);

                sql.append(condition.getColumn())
                        .append(" ")
                        .append(condition.getOperator());

                if (operatorRequiresValue(condition)) {
                    sql.append(" ?");
                }
            }
        }

        return sql.toString();
    }

    private boolean operatorRequiresValue(Condition condition) {
        return !"IS NULL".equals(condition.getOperator())
                && !"IS NOT NULL".equals(condition.getOperator());
    }
}

