package com.example.todoapp.dto;

import com.example.todoapp.model.Task;

/**
 * DTO for task responses (GET endpoints).
 * Represents the full task data returned to clients.
 */
public record TaskResponseDto(
        Integer id,
        String title,
        String description,
        boolean done
) {
    /**
     * Convert a Task entity to a TaskResponseDto.
     * @param task task entity
     * @return response DTO
     */
    public static TaskResponseDto fromEntity(Task task) {
        return new TaskResponseDto(
                task.id(),
                task.title(),
                task.description(),
                task.done()
        );
    }
}
