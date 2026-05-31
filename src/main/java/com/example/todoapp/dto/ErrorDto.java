package com.example.todoapp.dto;

/**
 * DTO for error responses.
 */
public record ErrorDto(
        String field,
        String message
) {
    /**
     * Create an error DTO for validation errors.
     * @param validationError validation error message (format: "field: message")
     * @return error DTO
     */
    public static ErrorDto fromValidationError(String validationError) {
        if (validationError.contains(":")) {
            String[] parts = validationError.split(":", 2);
            return new ErrorDto(parts[0].trim(), parts[1].trim());
        }
        return new ErrorDto("unknown", validationError);
    }

    /**
     * Create a generic error DTO.
     * @param message error message
     * @return error DTO
     */
    public static ErrorDto of(String message) {
        return new ErrorDto(null, message);
    }
}
