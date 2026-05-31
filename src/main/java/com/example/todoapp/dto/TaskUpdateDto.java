package com.example.todoapp.dto;

/**
 * DTO for updating an existing task (PUT /tasks/{id}).
 * All fields are required.
 */
public record TaskUpdateDto(
        String title,
        String description,
        Boolean done
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
        if (done == null) {
            return "done: must not be null";
        }
        return null; // Valid
    }
}
