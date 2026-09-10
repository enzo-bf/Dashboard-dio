package com.board.configuration;

import com.board.exception.PersistenceException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory implements TransactionExecutor {

    private final ApplicationProperties properties;

    public ConnectionFactory(ApplicationProperties properties) {
        this.properties = properties;
        loadDriver();
    }

    public Connection open() {
        try {
            Connection connection = DriverManager.getConnection(
                    properties.getUrl(),
                    properties.getUsername(),
                    properties.getPassword()
            );
            connection.setAutoCommit(true);
            return connection;
        } catch (SQLException exception) {
            throw new PersistenceException("Não foi possível obter conexão com o banco de dados.", exception);
        }
    }

    @Override
    public <T> T transactional(SqlFunction<T> work) {
        Connection connection = open();
        try {
            connection.setAutoCommit(false);
            T result = work.apply(connection);
            connection.commit();
            return result;
        } catch (RuntimeException exception) {
            rollbackQuietly(connection);
            throw exception;
        } catch (SQLException exception) {
            rollbackQuietly(connection);
            throw new PersistenceException("Falha ao executar transação.", exception);
        } finally {
            closeQuietly(connection);
        }
    }

    private void loadDriver() {
        try {
            Class.forName(properties.getDriver());
        } catch (ClassNotFoundException exception) {
            throw new PersistenceException("Driver JDBC não encontrado: " + properties.getDriver(), exception);
        }
    }

    private void rollbackQuietly(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
            }
        } catch (SQLException ignored) {
            // rollback best-effort
        }
    }

    private void closeQuietly(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // close best-effort
        }
    }

    @FunctionalInterface
    public interface SqlFunction<T> {
        T apply(Connection connection) throws SQLException;
    }
}
