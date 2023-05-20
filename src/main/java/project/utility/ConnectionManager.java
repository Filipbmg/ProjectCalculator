package project.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Database connection singleton
public class ConnectionManager {

    private static Connection connection = null;

    public static Connection getConnection(String dbUrl, String username, String password){
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(dbUrl, username, password);
            } catch (SQLException e) {
                System.out.println("Could not connect to database");
                e.printStackTrace();
            }
        }
        return connection;
    }
}