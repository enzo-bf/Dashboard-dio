package com.board.configuration;

import com.board.exception.PersistenceException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public final class ApplicationProperties {

    private static final String CLASSPATH_FILE = "application.properties";

    private final Properties properties;

    private ApplicationProperties(Properties properties) {
        this.properties = properties;
    }

    public static ApplicationProperties load() {
        Properties properties = new Properties();
        try (InputStream inputStream = ApplicationProperties.class.getClassLoader().getResourceAsStream(CLASSPATH_FILE)) {
            if (inputStream == null) {
                throw new PersistenceException(
                        "Arquivo application.properties não encontrado no classpath.",
                        new IllegalStateException(CLASSPATH_FILE)
                );
            }
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new PersistenceException("Falha ao carregar application.properties.", exception);
        }
        return new ApplicationProperties(properties);
    }

    public String getRequired(String key) {
        String environmentKey = key.toUpperCase().replace('.', '_');
        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Propriedade obrigatória não configurada: " + key);
        }
        return value.trim();
    }

    public String getUrl() {
        return getRequired("db.url");
    }

    public String getUsername() {
        return getRequired("db.username");
    }

    public String getPassword() {
        String environmentPassword = System.getenv("DB_PASSWORD");
        if (environmentPassword != null) {
            return environmentPassword;
        }
        return Objects.requireNonNullElse(properties.getProperty("db.password"), "");
    }

    public String getDriver() {
        return getRequired("db.driver");
    }

    public String getFlywayLocations() {
        return getRequired("flyway.locations");
    }
}
