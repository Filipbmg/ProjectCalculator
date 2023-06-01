package project.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import project.model.*;
import project.utility.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SubProjectRepository {
    //Import enviroment variables
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;


    public List<SubProject> getSubProjects(int projectId) {
        List<SubProject> subProjectList = new ArrayList<>();
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "SELECT subprojects.* FROM subprojects " +
                    "WHERE subprojects.project_id = ? " +
                    "ORDER BY subprojects.deadline";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, projectId);

            ResultSet results = preparedStatement.executeQuery();
            while (results.next()) {
                SubProject subProjectInfo = new SubProject();
                subProjectInfo.setSubProjectId(results.getInt("id"));
                subProjectInfo.setProjectId(results.getInt("project_id"));
                subProjectInfo.setSubProjectName(results.getString("subproject_name"));
                subProjectInfo.setSubProjectDescription(results.getString("subproject_description"));
                subProjectInfo.setStart(results.getDate("start").toLocalDate());
                subProjectInfo.setDeadline(results.getDate("deadline").toLocalDate());
                subProjectInfo.setTimeEstimate(results.getInt("time_estimate"));
                subProjectList.add(subProjectInfo);
            }
            return subProjectList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subProjectList;
    }

    public SubProject getSubProject(int subProjectId) {
        SubProject subproject = new SubProject();
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "SELECT subprojects.* FROM subprojects WHERE subprojects.id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, subProjectId);

            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                    subproject.setSubProjectId(results.getInt("id"));
                subproject.setProjectId(results.getInt("project_id"));
                subproject.setSubProjectName(results.getString("subproject_name"));
                subproject.setSubProjectDescription(results.getString("subproject_description"));
                subproject.setStart(results.getDate("start").toLocalDate());
                subproject.setDeadline(results.getDate("deadline").toLocalDate());
                subproject.setTimeEstimate(results.getInt("time_estimate"));
            }
            return subproject;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subproject;
    }

    public void subprojectUpdate(SubProject subProject) {
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "UPDATE subprojects SET subproject_description=?, start=?, deadline=? " +
                    "WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, subProject.getSubProjectDescription());
            preparedStatement.setDate(2, java.sql.Date.valueOf(subProject.getStart()));
            preparedStatement.setDate(3, java.sql.Date.valueOf(subProject.getDeadline()));
            preparedStatement.setInt(4, subProject.getSubProjectId());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void dbAddSubProject(SubProject subProject) {
        try {
            //Connect til db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Initialiser prepared statement
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO subprojects (subproject_name, project_id, start, deadline, subproject_description, time_estimate) VALUES (?, ?, ?, ?, ?, 0)");

            //set attributer i prepared statement
            preparedStatement.setString(1, subProject.getSubProjectName());
            preparedStatement.setInt(2, subProject.getProjectId());
            preparedStatement.setDate(3, java.sql.Date.valueOf(subProject.getStart()));
            preparedStatement.setDate(4, java.sql.Date.valueOf(subProject.getDeadline()));
            preparedStatement.setString(5, subProject.getSubProjectDescription());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteSubProject(int subProjectId) {
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            String query = "DELETE FROM subprojects WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, subProjectId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
