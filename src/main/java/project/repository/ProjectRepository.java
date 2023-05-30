package project.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import project.model.*;
import project.utility.ConnectionManager;

import javax.xml.transform.Result;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProjectRepository {
    //Import enviroment variables
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    public List<Project> getProjects(String loggedinUsername){
        List<Project> projectList = new ArrayList<>();
        try{
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekter brugeren er en del af
            String query = "SELECT projects.id, projects.project_name, projects.owner_id, projects.start, projects.deadline, project_description FROM projects " +
                    "JOIN project_users ON projects.id = project_users.project_id " +
                    "JOIN users ON project_users.user_id = users.id " +
                    "WHERE users.username = ? " +
                    "ORDER BY projects.deadline";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, loggedinUsername);
            ResultSet projectResults = preparedStatement.executeQuery();
            while (projectResults.next()) {
                Project projectInfo = new Project();
                projectInfo.setProjectId(projectResults.getInt("id"));
                projectInfo.setProjectName(projectResults.getString("project_name"));
                projectInfo.setProjectDescription(projectResults.getString("project_description"));
                projectInfo.setOwnerId(projectResults.getInt("owner_id"));
                projectInfo.setStart(projectResults.getDate("start").toLocalDate());
                projectInfo.setDeadline(projectResults.getDate("deadline").toLocalDate());
                projectList.add(projectInfo);
            }
            return projectList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projectList;
    }

    public void dbAddProject(Project project) {
        try {
            //Connect til db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Initialiser prepared statement
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO projects (project_name, owner_id, start, deadline, project_description, time_estimate) VALUES (?, ?, ?, ?, ?, 0)",
                    Statement.RETURN_GENERATED_KEYS);

            //set attributer i prepared statement
            preparedStatement.setString(1, project.getProjectName());
            preparedStatement.setInt(2, project.getOwnerId());
            preparedStatement.setDate(3, java.sql.Date.valueOf(project.getStart()));
            preparedStatement.setDate(4, java.sql.Date.valueOf(project.getDeadline()));
            preparedStatement.setString(5, project.getProjectDescription());

            //execute og tag det genereret project_id
            int rowTest = preparedStatement.executeUpdate();
            if (rowTest == 0) {
                throw new SQLException("Projektet blev ikke oprettet");
            }
            ResultSet primaryKey = preparedStatement.getGeneratedKeys();
            int projectId;
            if (primaryKey.next()) {
                projectId = primaryKey.getInt(1);
            } else {
                throw new SQLException("Projekt id blev ikke genereret");
            }

            //Brug det generet project_id til at forbinde brugeren med hans nyoprettede projekt
            PreparedStatement preparedStatement2 = connection.prepareStatement("INSERT INTO project_users (project_id, user_id) VALUES (?, ?)");
            preparedStatement2.setInt(1, projectId);
            preparedStatement2.setInt(2, project.getOwnerId());
            preparedStatement2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Project getProject(int projectId) {
        Project project = new Project();
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "SELECT projects.* FROM projects WHERE projects.id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, projectId);

            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                project.setProjectId(results.getInt("id"));
                project.setOwnerId(results.getInt("owner_id"));
                project.setProjectName(results.getString("project_name"));
                project.setProjectDescription(results.getString("project_description"));
                project.setStart(results.getDate("start").toLocalDate());
                project.setDeadline(results.getDate("deadline").toLocalDate());
                project.setTimeEstimate(results.getInt("time_estimate"));
            }
            return project;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return project;
    }

    public LocalDate getProjectDeadline(int projectId) {
        LocalDate deadline = null;
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "SELECT deadline FROM projects WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, projectId);

            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                deadline = result.getDate("deadline").toLocalDate();
            }
            return deadline;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deadline;
    }

    //Sletter brugers forbindelse til et projekt
    public void removeUser(int userId, int projectId) {
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            String query = "DELETE FROM project_users WHERE user_id = ? AND project_id =?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, projectId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Tilføjer bruger til projekts brugerliste
    public void addUserToProject(int userId, int projectId) {
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Undersøger om projectid + userid kombination allerede eksisterer
            String query = "SELECT * FROM project_users WHERE user_id = ? AND project_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, projectId);
            ResultSet result = preparedStatement.executeQuery();

            //Stopper metoden hvis kombinationen allerede eksisterer
            if (result.next()) {
                return;
            }

            //Tilføj
            query = "INSERT INTO project_users (user_id, project_id) VALUES (?, ?)";
            PreparedStatement preparedStatement2 = connection.prepareStatement(query);
            preparedStatement2.setInt(1, userId);
            preparedStatement2.setInt(2, projectId);
            preparedStatement2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteProject(int projectId) {
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            String query = "DELETE FROM projects WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, projectId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateProject(Project project) {
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            String query = "UPDATE projects SET project_description = ?, start = ?, deadline = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, project.getProjectDescription());
            preparedStatement.setDate(2, java.sql.Date.valueOf(project.getStart()));
            preparedStatement.setDate(3, java.sql.Date.valueOf(project.getDeadline()));
            preparedStatement.setInt(4, project.getProjectId());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}





