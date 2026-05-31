package com.example.todoapp.dao;

import com.example.todoapp.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Data Access Object for {@link Task} model using SQLite database.
 */
public class TaskDao {

    private static final Logger log = LoggerFactory.getLogger(TaskDao.class);
    private static final String DB_URL = "jdbc:sqlite:tasks.db";

    /**
     * Constructor - initializes the database and creates the table if needed.
     */
    public TaskDao() {
        initDatabase();
    }

    /**
     * Initialize database: create table if not exists and add sample data.
     */
    private void initDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                done INTEGER NOT NULL DEFAULT 0
            )
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            log.debug("Database table 'tasks' initialized");

            // Add sample data if table is empty
            if (count() == 0) {
                save(new Task(null, "Réviser DS de maths", "Séries numériques et probabilités.", false));
                save(new Task(null, "Valider mon PIVE", "PIVE Club Poker.", true));
                save(new Task(null, "Choisir mon parcours de 4A", "SIR ou SIA ?", false));
                log.debug("Sample tasks inserted");
            }

        } catch (SQLException e) {
            log.error("Error initializing database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Get a connection to the SQLite database.
     * @return database connection
     * @throws SQLException if connection fails
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Save a new task.
     * @param task task to save (id can be null, will be auto-generated)
     * @return saved task with generated ID
     */
    public Task save(Task task) {
        String sql = "INSERT INTO tasks (title, description, done) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, task.title());
            pstmt.setString(2, task.description());
            pstmt.setInt(3, task.done() ? 1 : 0);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    Task savedTask = new Task(generatedId, task.title(), task.description(), task.done());
                    log.debug("Task saved: id={}, title={}", generatedId, task.title());
                    return savedTask;
                } else {
                    throw new SQLException("Creating task failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            log.error("Error saving task", e);
            throw new RuntimeException("Failed to save task", e);
        }
    }

    /**
     * Find a task by its ID.
     * @param id task identifier
     * @return task wrapped in Optional, empty if not found
     */
    public Optional<Task> findById(int id) {
        String sql = "SELECT id, title, description, done FROM tasks WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("done") == 1
                );
                log.debug("Task found: id={}", id);
                return Optional.of(task);
            }

            log.debug("Task not found: id={}", id);
            return Optional.empty();

        } catch (SQLException e) {
            log.error("Error finding task by id: {}", id, e);
            throw new RuntimeException("Failed to find task", e);
        }
    }

    /**
     * Find all tasks.
     * @return collection of all tasks
     */
    public Collection<Task> findAll() {
        String sql = "SELECT id, title, description, done FROM tasks";
        Collection<Task> tasks = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("done") == 1
                );
                tasks.add(task);
            }

            log.debug("Found {} tasks", tasks.size());
            return tasks;

        } catch (SQLException e) {
            log.error("Error finding all tasks", e);
            throw new RuntimeException("Failed to find tasks", e);
        }
    }

    /**
     * Update an existing task.
     * @param id task identifier
     * @param task new task data
     * @return updated task wrapped in Optional, empty if not found
     */
    public Optional<Task> update(int id, Task task) {
        // Check if task exists
        if (findById(id).isEmpty()) {
            log.warn("Attempted to update non-existent task: id={}", id);
            return Optional.empty();
        }

        String sql = "UPDATE tasks SET title = ?, description = ?, done = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.title());
            pstmt.setString(2, task.description());
            pstmt.setInt(3, task.done() ? 1 : 0);
            pstmt.setInt(4, id);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                Task updatedTask = new Task(id, task.title(), task.description(), task.done());
                log.debug("Task updated: id={}", id);
                return Optional.of(updatedTask);
            }

            return Optional.empty();

        } catch (SQLException e) {
            log.error("Error updating task: id={}", id, e);
            throw new RuntimeException("Failed to update task", e);
        }
    }

    /**
     * Delete a task by ID.
     * @param id task identifier
     * @return true if deleted, false if not found
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                log.debug("Task deleted: id={}", id);
                return true;
            } else {
                log.warn("Attempted to delete non-existent task: id={}", id);
                return false;
            }

        } catch (SQLException e) {
            log.error("Error deleting task: id={}", id, e);
            throw new RuntimeException("Failed to delete task", e);
        }
    }

    /**
     * Count total number of tasks.
     * @return number of tasks
     */
    private int count() {
        String sql = "SELECT COUNT(*) FROM tasks";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            log.error("Error counting tasks", e);
            return 0;
        }
    }
}
