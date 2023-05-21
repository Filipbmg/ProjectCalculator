package project.repository;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import project.model.User;
import project.utility.ConnectionManager;

import jakarta.servlet.http.HttpSession;
import java.sql.*;

@Repository
public class UserRepository {

    //Import enviroment variables
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    public boolean checkIfDup(User user) {
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT username FROM users WHERE username = ?");

            //set attribute in prepared statement
            preparedStatement.setString(1, user.getUsername());

            //execute
            ResultSet result = preparedStatement.executeQuery();

            //returner true hvis brugernavnet er i databasen allerede
            return result.next();
        } catch (SQLException e) {
            System.out.println("Error while checking for duplicates");
            e.printStackTrace();
        }
        return false;
    }

    public void addUser(User user){
        try{
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users(username, password, first_name, last_name) VALUES (?, ?, ?, ?)");

            //set attributer i prepared statement
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getFirstName());
            preparedStatement.setString(4, user.getLastName());

            //execute
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error during user creation");
            e.printStackTrace();
        }
    }

    public boolean verifyLogin(User user) {
        try{
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT username, password FROM users WHERE username = ? AND password = ?");

            //set attribute in prepared statement
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());

            //execute
            ResultSet result = preparedStatement.executeQuery();

            return result.next();
        } catch (SQLException e) {
            System.out.println("Fejl under login");
            e.printStackTrace();
        }
        return false;
    }

}
