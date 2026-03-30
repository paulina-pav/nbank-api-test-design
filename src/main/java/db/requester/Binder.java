package db.requester;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Binder {

    public void bind(PreparedStatement statement, RequestSkeleton request) throws SQLException {
        int parameterIndex = 1;

        switch (request.getRequestType()) {
            case INSERT:
                bindValues(statement, request, parameterIndex);
                break;

            case UPDATE:
                parameterIndex = bindValues(statement, request, parameterIndex);
                bindConditions(statement, request, parameterIndex);
                break;

            case SELECT:
            case DELETE:
                bindConditions(statement, request, parameterIndex);
                break;

            default:
                throw new UnsupportedOperationException(
                        "Request type " + request.getRequestType() + " not implemented for binding"
                );
        }
    }

    private int bindValues(PreparedStatement statement, RequestSkeleton request, int parameterIndex)
            throws SQLException {
        if (request.getValues() != null) {
            for (Object value : request.getValues().values()) {
                statement.setObject(parameterIndex++, value);
            }
        }
        return parameterIndex;
    }

    private int bindConditions(PreparedStatement statement, RequestSkeleton request, int parameterIndex)
            throws SQLException {
        if (request.getConditions() != null) {
            for (Condition condition : request.getConditions()) {
                if (operatorRequiresValue(condition)) {
                    statement.setObject(parameterIndex++, condition.getValue());
                }
            }
        }
        return parameterIndex;
    }

    private boolean operatorRequiresValue(Condition condition) {
        return !"IS NULL".equals(condition.getOperator())
                && !"IS NOT NULL".equals(condition.getOperator());
    }
}
