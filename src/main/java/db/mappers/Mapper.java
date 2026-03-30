package db.mappers;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Mapper {
    public static <T> T map(ResultSet resultSet, Class<T> clazz) throws SQLException {
        if (!resultSet.next()) {
            return null;
        }

        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                String columnName = getColumnName(field);
                Object value = extractValue(resultSet, columnName, field.getType());

                field.set(instance, value);
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map ResultSet to " + clazz.getSimpleName(), e);
        }
    }

    private static String getColumnName(Field field) {
        ColumnName annotation = field.getAnnotation(ColumnName.class);
        if (annotation != null) {
            return annotation.value();
        }
        return field.getName();
    }

    private static Object extractValue(ResultSet resultSet, String columnName, Class<?> fieldType) throws SQLException {
        if (fieldType == Long.class) {
            long value = resultSet.getLong(columnName);
            return resultSet.wasNull() ? null : value;
        }
        if (fieldType == Integer.class) {
            int value = resultSet.getInt(columnName);
            return resultSet.wasNull() ? null : value;
        }
        if (fieldType == Double.class) {
            double value = resultSet.getDouble(columnName);
            return resultSet.wasNull() ? null : value;
        }
        if (fieldType == Boolean.class) {
            boolean value = resultSet.getBoolean(columnName);
            return resultSet.wasNull() ? null : value;
        }
        if (fieldType == String.class) {
            return resultSet.getString(columnName);
        }

        return resultSet.getObject(columnName);
    }
}
