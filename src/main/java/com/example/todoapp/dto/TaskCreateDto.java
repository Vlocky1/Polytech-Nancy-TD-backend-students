package com.example.todoapp.dto;

/**
 * DTO for creating a new task (POST /tasks).
 * Only title and description are required.
 * ID will be auto-generated and done defaults to false.
 */
public record TaskCreateDto(
        String title,
        String description
) {
    /**
     * Validate the DTO.
     * @return error message if invalid, null if valid
     */
    public String validate() {
        if (title == null || title.isBlank()) {
            return "title: must not be blank";
        }
        if (title.length() > 50) {
            return "title: maximum length is 50 characters";
        }
        if (description != null && description.length() > 255) {
            return "description: maximum length is 255 characters";
        }
        return null; // Valid
    }
}
