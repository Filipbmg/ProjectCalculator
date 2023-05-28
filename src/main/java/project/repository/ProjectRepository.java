package project.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import project.model.Project;
import project.model.SubProject;
import project.model.SubTask;
import project.model.Task;
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

    public void dbAddProject(Project project) {
        try{
            //Connect til db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Initialiser prepared statement
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO projects (project_name, owner_id, deadline) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            //set attributer i prepared statement
            preparedStatement.setString(1, project.getProjectName());
            preparedStatement.setInt(2, project.getOwnerId());
            preparedStatement.setDate(3, java.sql.Date.valueOf(project.getDeadline()));

            //execute
            int rowTest = preparedStatement.executeUpdate();
            if (rowTest == 0) {
                throw new SQLException("Projektet blev ikke oprettet");
            }

            //Undersøg om projektet blev oprettet og tag det genereret project_id
            ResultSet primaryKey = preparedStatement.getGeneratedKeys();
            int projectId;
            if (primaryKey.next()) {
                projectId = primaryKey.getInt(1);
            } else {
                throw new SQLException("Projekt id blev ikke genereret");
            }

            //Brug det generet project_id til at forbinde brugeren med hans nyoprettede projekt
            preparedStatement = connection.prepareStatement("INSERT INTO project_users (project_id, user_id) VALUES (?, ?)");

            preparedStatement.setInt(1, projectId);
            preparedStatement.setInt(2, project.getOwnerId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<Task> getTasks(int projectId) {
        List<Task> taskList = new ArrayList<>();
        try{
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "SELECT tasks.* FROM tasks " +
                    "WHERE tasks.project_id = ? " +
                    "ORDER BY tasks.deadline";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, projectId);

            ResultSet results = preparedStatement.executeQuery();
            while (results.next()) {
                Task taskInfo = new Task();
                taskInfo.setTaskId(results.getInt("id"));
                taskInfo.setProjectId(results.getInt("project_id"));
                taskInfo.setTaskName(results.getString("task_name"));
                taskInfo.setTaskDescription(results.getString("task_description"));
                taskInfo.setStart(results.getDate("start").toLocalDate());
                taskInfo.setDeadline(results.getDate("deadline").toLocalDate());
                taskInfo.setTimeEstimate(results.getInt("time_estimate"));
                taskList.add(taskInfo);
            }
            return taskList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return taskList;
    }

    public Project getProject(int projectId) {
        Project project = new Project();
        try{
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
                project.setDeadline(results.getDate("deadline").toLocalDate());
                project.setTimeEstimate(results.getInt("time_estimate"));
            }
            return project;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return project;
    }

    public List<SubProject> getSubProjects(int projectId) {
        List<SubProject> subProjectList = new ArrayList<>();
        try{
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

    public void updateTask(Task task) {
        try{
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "UPDATE tasks SET task_name=?, task_description=?, start=?, deadline=?, time_estimate=? " +
                    "WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, task.getTaskName());
            preparedStatement.setString(2, task.getTaskDescription());
            preparedStatement.setDate(3, Date.valueOf(task.getStart()));
            preparedStatement.setDate(4, Date.valueOf(task.getDeadline()));
            preparedStatement.setInt(5, task.getTimeEstimate());
            preparedStatement.setInt(6, task.getTaskId());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSubproject(int subprojectId) {
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "UPDATE subprojects SET subproject_name=?, subproject_description=?, start=?, deadline=?, time_estimate=? " +
                    "WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, subprojectId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

        public Task getTask(int taskId) {
            Task task = new Task();
            try{
                //connect to db
                Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

                //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
                String query = "SELECT tasks.* FROM tasks WHERE id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, taskId);

                ResultSet results = preparedStatement.executeQuery();
                if (results.next()) {
                    task.setTaskId(results.getInt("id"));
                    task.setProjectId(results.getInt("project_id"));
                    task.setTaskName(results.getString("task_name"));
                    task.setTaskDescription(results.getString("task_description"));
                    task.setStart(results.getDate("start").toLocalDate());
                    task.setDeadline(results.getDate("deadline").toLocalDate());
                    task.setTimeEstimate(results.getInt("time_estimate"));
                }
                return task;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return task;
        }
    public void deleteTask(int taskId) {
        try{
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "DELETE FROM tasks WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, taskId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<SubTask> getSubTasks(int subProjectId) {
        List<SubTask> subTaskList = new ArrayList<>();
        try{
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "SELECT subtasks.* FROM subtasks " +
                    "WHERE subtasks.subproject_id = ? " +
                    "ORDER BY subtasks.deadline";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, subProjectId);

            ResultSet results = preparedStatement.executeQuery();
            while (results.next()) {
                SubTask subTaskInfo = new SubTask();
                subTaskInfo.setSubTaskId(results.getInt("id"));
                subTaskInfo.setSubProjectId(results.getInt("subproject_id"));
                subTaskInfo.setSubTaskName(results.getString("subtask_name"));
                subTaskInfo.setSubTaskDescription(results.getString("subtask_description"));
                subTaskInfo.setStart(results.getDate("start").toLocalDate());
                subTaskInfo.setDeadline(results.getDate("deadline").toLocalDate());
                subTaskInfo.setTimeEstimate(results.getInt("time_estimate"));
                subTaskList.add(subTaskInfo);
            }
            return subTaskList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subTaskList;
    }

    public SubProject getSubProject(int subProjectId) {
        SubProject subproject = new SubProject();
        try{
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

    public SubTask getSubTask(int subTaskId) {
        SubTask subTask = new SubTask();
        try{
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "SELECT subtasks.* FROM subtasks WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, subTaskId);

            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                subTask.setSubTaskId(results.getInt("id"));
                subTask.setSubProjectId(results.getInt("subproject_id"));
                subTask.setSubTaskName(results.getString("subtask_name"));
                subTask.setSubTaskDescription(results.getString("subtask_description"));
                subTask.setStart(results.getDate("start").toLocalDate());
                subTask.setDeadline(results.getDate("deadline").toLocalDate());
                subTask.setTimeEstimate(results.getInt("time_estimate"));
            }
            return subTask;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subTask;
    }

    public void updateSubTask(SubTask subTask) {
        try{
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "UPDATE subtasks SET subtask_name=?, subtask_description=?, start=?, deadline=?, time_estimate=? " +
                    "WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, subTask.getSubTaskName());
            preparedStatement.setString(2, subTask.getSubTaskDescription());
            preparedStatement.setDate(3, Date.valueOf(subTask.getStart()));
            preparedStatement.setDate(4, Date.valueOf(subTask.getDeadline()));
            preparedStatement.setInt(5, subTask.getTimeEstimate());
            preparedStatement.setInt(6, subTask.getSubTaskId());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteSubTask(int subTaskId) {
        try{
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Brug brugernavn til at få projekt id, navn og projekt ejer id på alle projekter brugeren er en del af
            String query = "DELETE FROM subtasks WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, subTaskId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public LocalDate getProjectDeadline(int projectId) {
            LocalDate deadline = null;
        try{
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

}





