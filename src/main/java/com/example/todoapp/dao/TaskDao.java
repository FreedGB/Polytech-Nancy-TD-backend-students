package com.example.todoapp.dao;

import com.example.todoapp.business.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object for {@link Task} model.
 */
public class TaskDao {

    private static final Logger log = LoggerFactory.getLogger(TaskDao.class);
    private final String databaseURL = "JDBC:sqlite:task_database.db";

    public TaskDao () {
        try {
            createTableIfNotExists();
            initializeTable();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public void createTableIfNotExists() throws SQLException {
        String sql = """
                        CREATE TABLE IF NOT EXISTS Tasks (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            title VARCHAR(20) NOT NULL,
                            description VARCHAR(255),
                            done BOOL
                        );""";

        try (Connection connection = DriverManager.getConnection(databaseURL)){
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            statement.close();
        }
    }

    public void initializeTable() {
        save(new Task(1, "Réviser DS de maths", "Séries numériques et probabilités.", false));
        save(new Task(2, "Valider mon PIVE", "PIVE Club Poker.", true));
        save(new Task(3, "Choisir mon parcours de 4A", "SIR ou SIA ?", false));
    }

    /**
     * Persist {@link Task} model.
     * @param task task to save.
     * @return task model.
     */
    public Task save(Task task) {
        String sql = """
                        INSERT INTO Tasks (title, description, done)
                        VALUES (?, ?, ?);""";

        try (Connection connection = DriverManager.getConnection(databaseURL)){
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, task.title());
            statement.setString(2, task.description());
            statement.setBoolean(3, task.done());
            statement.execute();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        return task;
    }

    /**
     * Retrieve {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model wrapped by Optional.
     */
    public Optional<Task> findById(int id) {
        String sql = """
                        SELECT *
                        FROM Tasks
                        WHERE id = ?;
                     """;
        Optional<Task> task = Optional.empty();

        try (Connection connection = DriverManager.getConnection(databaseURL)){
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            task = buildTaskModel(rs);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return task;
    }

    /**
     * Retrieve the list of all {@link Task}s.
     * @return {@link Task} collection
     */
    public Collection<Task> getTasksList(boolean todoOnly) {
        Collection<Task> tasksList = new ArrayList<>();

        String sql;

        if (todoOnly) {
            sql = "SELECT * FROM Tasks WHERE done = true";
        }
        else {
            sql = "SELECT * FROM Tasks;";
        }

        try (Connection connection = DriverManager.getConnection(databaseURL)){
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                tasksList.add(buildTaskModel(rs).get());
            }

        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        return tasksList;
    }

    /**
     * Remove a {@link Task} by its id
     * @param id identifier of the {@link Task}.
     */
    public void deleteTaskById(int id) {
        String sql = """
                        DELETE *
                        FROM Tasks
                        WHERE id = ?;
                     """;

        try (Connection connection = DriverManager.getConnection(databaseURL)){
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeQuery();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Update a {@link Task} by its id
     * @param id identifier of the {@link Task}.
     */
    public void updateTaskById(int id, String title, String description, boolean done) {
        String sql = """
                        UPDATE Tasks
                        SET title = ?, description = ?, done = ?
                        WHERE id = ?;
                     """;

        try (Connection connection = DriverManager.getConnection(databaseURL)){
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.setString(1, title);
            statement.setString(2, description);
            statement.setBoolean(3, done);
            statement.executeQuery();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public Optional<Task> buildTaskModel(ResultSet rs) throws SQLException{
        Task task = new Task(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getBoolean("done")
        );

        return Optional.of(task);
    }
}
