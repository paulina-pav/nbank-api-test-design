package db.requester;

import api.configs.Config;
import db.mappers.Mapper;

import java.sql.*;


public class Executor {
    private final SqlQueryCreator queryCreator = new SqlQueryCreator();
    private final Binder binder = new Binder();

    public <T> T extractAs(RequestSkeleton request, Class<T> clazz) {
        return executeQuery(request, clazz);
    }

    private <T> T executeQuery(RequestSkeleton request, Class<T> clazz) {
        String sql = queryCreator.buildSQL(request);

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            binder.bind(statement, request);

            try (ResultSet resultSet = statement.executeQuery()) {
                return mapResult(resultSet, clazz);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database query failed", e);
        }
    }

    private <T> T mapResult(ResultSet resultSet, Class<T> clazz) throws SQLException {
        return Mapper.map(resultSet, clazz);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                Config.getProperty("db.url"),
                Config.getProperty("db.username"),
                Config.getProperty("db.password")
        );
    }


    /*
    метод для исполнения др запросов. возвращает колво затронутых строк
     */
    public int execute(RequestSkeleton request) {
        String sql = queryCreator.buildSQL(request);

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            binder.bind(statement, request);
            return statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Database update failed", e);
        }
    }
}
