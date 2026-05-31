package com.example.todoapp.controller;

import com.example.todoapp.JsonUtils;
import com.example.todoapp.dto.ErrorDto;
import com.example.todoapp.dto.TaskCreateDto;
import com.example.todoapp.dto.TaskResponseDto;
import com.example.todoapp.dto.TaskUpdateDto;
import com.example.todoapp.service.TaskService;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

/**
 * Controller handling HTTP requests for Task endpoints.
 */
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Handle all /tasks requests.
     */
    public void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        log.info("Received {} request on {}", method, path);

        try {
            // POST /tasks
            if ("POST".equals(method) && "/tasks".equals(path)) {
                handleCreateTask(exchange);
                return;
            }

            // GET /tasks
            if ("GET".equals(method) && "/tasks".equals(path)) {
                handleGetAllTasks(exchange);
                return;
            }

            Matcher m = ID_PATH.matcher(path);

            // GET /tasks/{id}
            if ("GET".equals(method) && m.matches()) {
                handleGetTaskById(exchange, Integer.parseInt(m.group(1)));
                return;
            }

            // PUT /tasks/{id}
            if ("PUT".equals(method) && m.matches()) {
                handleUpdateTask(exchange, Integer.parseInt(m.group(1)));
                return;
            }

            // DELETE /tasks/{id}
            if ("DELETE".equals(method) && m.matches()) {
                handleDeleteTask(exchange, Integer.parseInt(m.group(1)));
                return;
            }

            sendResponse(exchange, 404, null);

        } catch (Exception e) {
            log.error("Unexpected error", e);
            ErrorDto error = ErrorDto.of("Internal server error");
            sendResponse(exchange, 500, JsonUtils.serialize(error));
        }
    }

    private void handleCreateTask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);

        TaskCreateDto dto = JsonUtils.deserialize(body, TaskCreateDto.class);

        // Validation
        String validationError = dto.validate();
        if (validationError != null) {
            ErrorDto error = ErrorDto.fromValidationError(validationError);
            sendResponse(exchange, 400, JsonUtils.serialize(error));
            return;
        }

        TaskResponseDto created = taskService.createTask(dto);
        exchange.getResponseHeaders().add("Location", "/tasks/" + created.id());
        sendResponse(exchange, 201, JsonUtils.serialize(created));
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        boolean todoOnly = nonNull(query) && query.contains("todo-only=true");

        Collection<TaskResponseDto> tasks = taskService.findAllTasks(todoOnly);

        if (tasks.isEmpty()) {
            sendResponse(exchange, 204, null);
        } else {
            sendResponse(exchange, 200, JsonUtils.serialize(tasks));
        }
    }

    private void handleGetTaskById(HttpExchange exchange, int id) throws IOException {
        Optional<TaskResponseDto> task = taskService.findTaskById(id);

        if (task.isPresent()) {
            sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
        } else {
            sendResponse(exchange, 404, null);
        }
    }

    private void handleUpdateTask(HttpExchange exchange, int id) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);

        TaskUpdateDto dto = JsonUtils.deserialize(body, TaskUpdateDto.class);

        // Validation
        String validationError = dto.validate();
        if (validationError != null) {
            ErrorDto error = ErrorDto.fromValidationError(validationError);
            sendResponse(exchange, 400, JsonUtils.serialize(error));
            return;
        }

        Optional<TaskResponseDto> updated = taskService.updateTask(id, dto);

        if (updated.isPresent()) {
            sendResponse(exchange, 200, JsonUtils.serialize(updated.get()));
        } else {
            sendResponse(exchange, 404, null);
        }
    }

    private void handleDeleteTask(HttpExchange exchange, int id) throws IOException {
        boolean deleted = taskService.deleteTask(id);

        if (deleted) {
            sendResponse(exchange, 204, null);
        } else {
            sendResponse(exchange, 404, null);
        }
    }

    private void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if (nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        }
    }
}
