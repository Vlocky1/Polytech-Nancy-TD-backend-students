package com.example.todoapp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for {@link Task} model.
 */
public class TaskDao {

    private final Map<Integer, Task> storage = new HashMap<>();

    {
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
        storage.put(task.id(), task);
        return task;
    }

    /**
     * Retrieve {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model wrapped by Optional.
     */
    public Optional<Task> findById(int id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Retrieve all {@link Task} models.
     * @return Collection of all tasks.
     */
    public Collection<Task> findAll() {
        return storage.values();
    }

    /**
     * Update an existing {@link Task}.
     * @param id   identifier of the task to update
     * @param task new task data
     * @return updated task wrapped by Optional, empty if task not found
     */
    public Optional<Task> update(int id, Task task) {
        if (!storage.containsKey(id)) {
            return Optional.empty();
        }
        Task updatedTask = new Task(id, task.title(), task.description(), task.done());
        storage.put(id, updatedTask);
        return Optional.of(updatedTask);
    }

    /**
     * Delete a {@link Task} by id.
     * @param id identifier of the task to delete
     * @return true if task was deleted, false if not found
     */
    public boolean delete(int id) {
        return storage.remove(id) != null;
    }
}
