package com.example.todoapp;

import com.example.todoapp.controller.TaskController;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.service.TaskService;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Main class of the application.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        // Initialize layers
        TaskDao taskDao = new TaskDao();
        TaskService taskService = new TaskService(taskDao);
        TaskController taskController = new TaskController(taskService);

        log.info("Application layers initialized");

        // Start HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/tasks", taskController::handleTasks);
        server.setExecutor(null);
        server.start();

        log.info("HTTP server started on http://localhost:8080");
    }
}
