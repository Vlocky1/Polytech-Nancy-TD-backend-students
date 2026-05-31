package com.example.todoapp.service;

import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.dto.TaskCreateDto;
import com.example.todoapp.dto.TaskResponseDto;
import com.example.todoapp.dto.TaskUpdateDto;
import com.example.todoapp.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for Task business logic.
 */
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final TaskDao taskDao;

    public TaskService(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    /**
     * Create a new task from DTO.
     * @param dto task creation DTO
     * @return created task as response DTO
     */
    public TaskResponseDto createTask(TaskCreateDto dto) {
        log.info("Creating new task: {}", dto.title());

        // Convert DTO to Entity (done defaults to false)
        Task task = new Task(null, dto.title(), dto.description(), false);
        Task savedTask = taskDao.save(task);

        return TaskResponseDto.fromEntity(savedTask);
    }

    /**
     * Find all tasks, optionally filtering by completion status.
     * @param todoOnly if true, return only incomplete tasks
     * @return collection of task response DTOs
     */
    public Collection<TaskResponseDto> findAllTasks(boolean todoOnly) {
        log.info("Finding all tasks, todoOnly={}", todoOnly);
        Collection<Task> tasks = taskDao.findAll();

        if (todoOnly) {
            tasks = tasks.stream()
                    .filter(task -> !task.done())
                    .collect(Collectors.toList());
        }

        return tasks.stream()
                .map(TaskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Find a task by its ID.
     * @param id task identifier
     * @return task response DTO wrapped in Optional
     */
    public Optional<TaskResponseDto> findTaskById(int id) {
        log.info("Finding task by id: {}", id);
        return taskDao.findById(id)
                .map(TaskResponseDto::fromEntity);
    }

    /**
     * Update an existing task from DTO.
     * @param id task identifier
     * @param dto task update DTO
     * @return updated task response DTO wrapped in Optional
     */
    public Optional<TaskResponseDto> updateTask(int id, TaskUpdateDto dto) {
        log.info("Updating task: id={}", id);

        // Convert DTO to Entity
        Task task = new Task(id, dto.title(), dto.description(), dto.done());

        return taskDao.update(id, task)
                .map(TaskResponseDto::fromEntity);
    }

    /**
     * Delete a task.
     * @param id task identifier
     * @return true if deleted, false otherwise
     */
    public boolean deleteTask(int id) {
        log.info("Deleting task: id={}", id);
        return taskDao.delete(id);
    }
}
