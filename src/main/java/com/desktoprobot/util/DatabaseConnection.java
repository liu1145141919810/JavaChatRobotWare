// File: src/main/java/com/desktoprobot/util/DatabaseConnection.java

package com.desktoprobot.util;


import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static String url;
    private static String username;
    private static String password;

    // 静态代码块，在类加载时读取配置
    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            url = prop.getProperty("database.url");
            username = prop.getProperty("database.username");
            password = prop.getProperty("database.password");

            // 加载PostgreSQL JDBC驱动
            Class.forName("org.postgresql.Driver");

        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError("Failed to load database configuration");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
