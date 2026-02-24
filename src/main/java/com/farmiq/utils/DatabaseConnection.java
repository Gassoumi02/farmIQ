package com.farmiq.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final Logger logger = LogManager.getLogger(DatabaseConnection.class);
    
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    
    private static Connection connection;
    
    static {
        loadConfiguration();
    }
    
    private static void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnection.class.getResourceAsStream("/config.properties")) {
            if (input == null) {
                logger.warn("config.properties non trouvé, utilisation des valeurs par défaut");
                setDefaultConfig();
                return;
            }
            props.load(input);
            
            // Environment variables take precedence over config.properties for production
            String host = resolveConfig("DB_HOST", "db.host", props, "localhost");
            String port = resolveConfig("DB_PORT", "db.port", props, "3306");
            String dbName = resolveConfig("DB_NAME", "db.name", props, "farmiq");
            
            URL = "jdbc:mysql://" + host + ":" + port + "/" + dbName 
                    + "?useSSL=false&serverTimezone=Africa/Tunis&characterEncoding=UTF-8"
                    + "&allowPublicKeyRetrieval=true";
            USER = resolveConfig("DB_USER", "db.user", props, "root");
            PASSWORD = resolveConfig("DB_PASSWORD", "db.password", props, "");
            
            logger.info("Configuration DB chargée: host={}, db={}", host, dbName);
        } catch (IOException e) {
            logger.warn("Erreur lecture config.properties, utilisation valeurs défaut", e);
            setDefaultConfig();
        }
    }
    
    /**
     * Resolves a configuration value: environment variable > config property > default.
     */
    private static String resolveConfig(String envVar, String propKey, Properties props, String defaultValue) {
        String envValue = System.getenv(envVar);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return props.getProperty(propKey, defaultValue);
    }
    
    private static void setDefaultConfig() {
        URL = "jdbc:mysql://localhost:3306/farmiq?useSSL=false&serverTimezone=Africa/Tunis&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
        USER = "root";
        PASSWORD = "";
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                logger.debug("Nouvelle connexion DB établie");
            } catch (ClassNotFoundException e) {
                logger.error("Driver MySQL introuvable", e);
                throw new SQLException("Driver MySQL introuvable: " + e.getMessage());
            }
        }
        return connection;
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Test connexion échoué", e);
            return false;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Connexion DB fermée");
            } catch (SQLException e) {
                logger.error("Erreur fermeture connexion", e);
            }
        }
    }
}
