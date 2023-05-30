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
public class SubTaskRepository {
    //Import enviroment variables
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    public void updateSubTask(SubTask subTask) {
        try {
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

    public SubTask getSubTask(int subTaskId) {
        SubTask subTask = new SubTask();
        try {
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

    public void deleteSubTask(int subTaskId) {
        try {
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

    public void createSubTask(SubTask subTask) {
        try {
            //connect to db
            Connection connection = ConnectionManager.getConnection(dbUrl, username, password);

            //Set attributer
            String query = "INSERT INTO subtasks (subproject_id, subtask_name, subtask_description, start, deadline, time_estimate) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, subTask.getSubProjectId());
            preparedStatement.setString(2, subTask.getSubTaskName());
            preparedStatement.setString(3, subTask.getSubTaskDescription());
            preparedStatement.setDate(4, java.sql.Date.valueOf(subTask.getStart()));
            preparedStatement.setDate(5, java.sql.Date.valueOf(subTask.getDeadline()));
            preparedStatement.setInt(6, subTask.getTimeEstimate());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<SubTask> getSubTasks(int subProjectId) {
        List<SubTask> subTaskList = new ArrayList<>();
        try {
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
}
