// File: src/main/java/com/desktoprobot/util/DatabaseConnection.java

package com.desktoprobot.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection
 * ----------------------------------------------------
 * 数据库连接工具类（JDBC 工具类）
 *
 * 功能说明：
 * 1. 在类加载时从 database.properties 配置文件中读取数据库连接信息
 * 2. 加载 PostgreSQL JDBC 驱动
 * 3. 对外提供统一的数据库连接获取方法
 *
 * 设计目的：
 * - 将数据库连接配置与业务代码解耦
 * - 统一管理数据库连接方式
 * - 避免在代码中硬编码数据库地址、用户名和密码
 *
 * 使用方式：
 * Connection conn = DatabaseConnection.getConnection();
 */
public class DatabaseConnection {
    /** 数据库连接 URL（如 jdbc:postgresql://localhost:5432/dbname） */
    private static String url;
    /** 数据库登录用户名 */
    private static String username;
    /** 数据库登录密码 */
    private static String password;
    /**
     * 静态代码块
     * ------------------------------------------------
     * 在类第一次被加载时执行，仅执行一次
     *
     * 主要完成：
     * 1. 读取 database.properties 配置文件
     * 2. 初始化数据库连接参数
     * 3. 加载 PostgreSQL JDBC 驱动
     */
    static {
        // 通过类加载器读取 resources 目录下的 database.properties 文件
        try (InputStream input =
                     DatabaseConnection.class
                             .getClassLoader()
                             .getResourceAsStream("database.properties")) {

            // 用于存储配置文件中的键值对
            Properties prop = new Properties();

            // 加载配置文件内容
            prop.load(input);

            // 读取数据库连接配置
            url = prop.getProperty("database.url");
            username = prop.getProperty("database.username");
            password = prop.getProperty("database.password");

            // 显式加载 PostgreSQL JDBC 驱动
            // （虽然 JDBC 4 以后可以自动加载，但显式加载更清晰、兼容性更好）
            Class.forName("org.postgresql.Driver");

        } catch (IOException | ClassNotFoundException ex) {
            // 配置文件读取失败或驱动加载失败时打印异常
            ex.printStackTrace();

            // 抛出初始化错误，阻止系统在数据库不可用情况下继续运行
            throw new ExceptionInInitializerError("Failed to load database configuration");
        }
    }

    /**
     * 获取数据库连接
     *
     * @return Connection 数据库连接对象
     * @throws SQLException 当数据库连接失败时抛出异常
     *
     * 调用示例：
     * try (Connection conn = DatabaseConnection.getConnection()) {
     *     // 执行 SQL 操作
     * }
     */
    public static Connection getConnection() throws SQLException {
        // 使用 DriverManager 创建并返回数据库连接
        return DriverManager.getConnection(url, username, password);
    }
}
