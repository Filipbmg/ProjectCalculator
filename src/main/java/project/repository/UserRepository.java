package project.repository;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import project.model.User;
import project.model.Project;
import project.utility.ConnectionManager;

import java.sql.*;
import java.time.LocalDate;
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
            e.printStackTrace();
        }
    }

    public boolean verifyLogin(User user) {
        passwordEncoder(user.getPassword());
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
            e.printStackTrace();
        }
        return false;
    }

    public List<Project> getProjects(User user){
        List<Project> projectList = new ArrayList<>();
        try{
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "SELECT projects.id, projects.project_name, projects.owner_id, projects.deadline FROM projects " +
                    "JOIN project_users ON projects.id = project_users.project_id " +
                    "JOIN users ON project_users.user_id = users.id " +
                    "WHERE users.username = ? " +
                    "ORDER BY projects.deadline";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user.getUsername());
            ResultSet projectResults = preparedStatement.executeQuery();
            while (projectResults.next()) {
                int projectId = projectResults.getInt("id");
                String projectName = projectResults.getString("project_name");
                int ownerId = projectResults.getInt("owner_id");
                LocalDate deadline = projectResults.getDate("deadline").toLocalDate();
                Project projectInfo = new Project();
                projectInfo.setProjectId(projectId);
                projectInfo.setProjectName(projectName);
                projectInfo.setOwnerId(ownerId);
                projectInfo.setDeadline(deadline);
                projectList.add(projectInfo);
            }
            return projectList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projectList;
    }

    public int getUserId(User user) {
        int userId = 0;
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM users WHERE username = ?");

            //set attribute in prepared statement
            preparedStatement.setString(1, user.getUsername());

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

}
