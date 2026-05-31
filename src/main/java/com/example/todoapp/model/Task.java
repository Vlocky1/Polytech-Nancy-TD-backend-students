package com.example.todoapp.model;

/**
 * Entity representing a Task.
 * @param id identifier (can be null for new tasks)
 * @param title task title
 * @param description task description
 * @param done completion status
 */
public record Task(Integer id, String title, String description, boolean done) {
}
