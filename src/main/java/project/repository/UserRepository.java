package project.repository;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import project.model.User;
import project.model.Project;
import project.utility.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {

    //Import enviroment variables
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    //Højere tal = stærkere encryption, vi holder den på 1 i denne omgang.
    public String passwordEncoder (String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(1);
        return encoder.encode(password);
    }
    public boolean checkIfUserExists(String newUsername) {
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT username FROM users WHERE username = ?");

            //set attributer i prepared statement
            preparedStatement.setString(1, newUsername);

            //execute
            ResultSet result = preparedStatement.executeQuery();

            //returner true hvis brugernavnet er i databasen allerede
            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUser(User user){
        try{
            //connect til db
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
            e.printStackTrace();
        }
    }

    public boolean verifyLogin(String verifyUsername, String verifyPassword) {
        String encodedPassword = passwordEncoder(verifyPassword);
        try{
            //connect til db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT username, password FROM users WHERE username = ? AND password = ?");

            //set attributer i prepared statement
            preparedStatement.setString(1, verifyUsername);
            preparedStatement.setString(2, encodedPassword);

            //execute
            ResultSet result = preparedStatement.executeQuery();

            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getUserId(String loggedinUsername) {
        int userId = 0;
        try {
            //connect til db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM users WHERE username = ?");

            //set attribut i prepared statement
            preparedStatement.setString(1, loggedinUsername);

            //execute
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                userId = result.getInt("id");
            }
            return userId;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    public List<User> getUserList(int projectId) {
        List<User> userList = new ArrayList<>();
        try{
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug projekt ID til at få en liste af tilknyttede brugere
            String query = "SELECT users.id, users.first_name, users.last_name FROM users, project_users " +
                    "WHERE users.id = project_users.user_id " +
                    "AND project_users.project_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, projectId);
            ResultSet userResults = preparedStatement.executeQuery();
            while (userResults.next()) {
                User userInfo = new User();
                userInfo.setUserId(userResults.getInt("id"));
                userInfo.setFirstName(userResults.getString("first_name"));
                userInfo.setLastName(userResults.getString("last_name"));
                userList.add(userInfo);
            }
            return userList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

}
