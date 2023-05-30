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
public class TaskRepository {
    //Import enviroment variables
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;



    public void createTask(Task task) {
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Set attributer
            String query = "INSERT INTO tasks (project_id, task_name, task_description, start, deadline, time_estimate) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, task.getProjectId());
            preparedStatement.setString(2, task.getTaskName());
            preparedStatement.setString(3, task.getTaskDescription());
            preparedStatement.setDate(4, java.sql.Date.valueOf(task.getStart()));
            preparedStatement.setDate(5, java.sql.Date.valueOf(task.getDeadline()));
            preparedStatement.setInt(6, task.getTimeEstimate());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTask(Task task) {
        try {
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


    public Task getTask(int taskId) {
        Task task = new Task();
        try {
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
        try {
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

    public List<Task> getTasks(int projectId) {
        List<Task> taskList = new ArrayList<>();
        try {
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
}
