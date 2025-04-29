package ru.practicum.kanban.httpServer.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.kanban.exceptions.MethodNotAllowedException;
import ru.practicum.kanban.exceptions.NotFoundException;
import ru.practicum.kanban.exceptions.TaskIsOverlapException;
import ru.practicum.kanban.httpServer.adapters.DurationAdapter;
import ru.practicum.kanban.httpServer.adapters.LocalDateTimeAdapter;
import ru.practicum.kanban.httpServer.RequestMethod;
import ru.practicum.kanban.httpServer.adapters.RuntimeTypeAdapterFactory;
import ru.practicum.kanban.model.Task;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.SubTask;
import ru.practicum.kanban.model.enums.TaskType;
import ru.practicum.kanban.service.TaskManager;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;


import static ru.practicum.kanban.httpServer.RequestMethod.*;

public class TaskHandler extends BaseHttpHandler {
    private final TaskManager manager;
    protected final Gson gson;
    private final TaskType taskType;

    // Runtime type adapter to handle polymorphic (Task/Epic/SubTask) JSON serialization.
    RuntimeTypeAdapterFactory<Task> taskAdapter = RuntimeTypeAdapterFactory
            .of(Task.class, "type")
            .registerSubtype(Task.class, "TASK")
            .registerSubtype(Epic.class, "EPIC")
            .registerSubtype(SubTask.class, "SUBTASK");

    public TaskHandler(TaskManager manager, TaskType taskType) {
        this.manager = manager;
        this.taskType = taskType;
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapterFactory(taskAdapter)
                .setPrettyPrinting()
                .create();
    }

    // Parses request body as JSON and adds or updates a task.
    private void addTask(HttpExchange exchange) throws IOException {
        String body;
        try (InputStream in = exchange.getRequestBody()) {
            byte[] bytes = in.readAllBytes();
            body = new String(bytes, StandardCharsets.UTF_8);
        }

        if (body.isEmpty()) {
            sendNotFound(exchange, "Incorrect data");
            return;
        }

        Task task;
        try {
            task = gson.fromJson(body, Task.class);
        } catch (JsonSyntaxException ex) {
            sendBadRequest(exchange);
            return;
        }

        try {
            if (manager.isExist(task)) {
                manager.update(task);
                sendText(exchange, "Task successfully updated.", 201);
            } else {
                manager.addTask(task);
                sendText(exchange, "Task successfully added.", 201);
            }
        } catch (TaskIsOverlapException e) {

            sendHasIntersection(exchange, e.getMessage());
        }
    }

    private void deleteTask(HttpExchange exchange, int id) throws IOException {
        try {
            manager.removeTaskById(id);
            sendText(exchange, "Task was successfully deleted", 200);
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    // Parses the request URI and HTTP method to a RequestMethod enum.
    // Throws NotFoundException or MethodNotAllowedException on invalid path or method.
    private RequestMethod getRequestMethod(String path, String requestMethod)
            throws NotFoundException, MethodNotAllowedException {

        String[] segments = path.split("/");
        // segments[0] is empty because path starts with '/'
        boolean validResource = segments.length >= 2 &&
                (segments[1].equals("tasks")
                        || segments[1].equals("epics")
                        || segments[1].equals("subtasks"));
        if (!validResource || segments.length > 4) {
            throw new NotFoundException("URI not recognized: " + path);
        }

        switch (requestMethod) {
            case "GET":
                if (segments.length == 2) {
                    return GET_TASKS;
                } else if (segments.length == 3) {
                    return GET_BY_ID;
                } else { // segments.length == 4 for /epics/{id}/subtasks
                    return GET_SUBTASKS_BY_EPIC_ID;
                }

            case "POST":
                return ADD_TASK;

            case "DELETE":
                if (segments.length == 3) {
                    return DELETE_BY_ID;
                } else {
                    throw new MethodNotAllowedException(
                            "DELETE not supported on this URI: " + path);
                }

            default:
                throw new MethodNotAllowedException(
                        "Method " + requestMethod + " not supported for " + path);
        }
    }

    private void getTasks(HttpExchange exchange) throws IOException {
        List<Task> list = switch (taskType) {
            case TASK -> manager.getTasks();
            case EPIC -> manager.getEpics();
            case SUBTASK -> manager.getSubTasks();
        };

        if (list.isEmpty()) {
            sendNotFound(exchange, "Tasks not found");
        } else {
            sendText(exchange, gson.toJson(list), 200);
        }
    }

    private void getTask(HttpExchange exchange, int id) throws IOException {
        try {
            Task task = manager.getTaskById(id);

            // Ensure the task type matches this handler
            if (task.getTaskType() != this.taskType) {
                throw new NoSuchElementException(
                        "Task with ID " + id + " not found in " + this.taskType
                );
            }
            sendText(exchange, gson.toJson(task), 200);

        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void getSubTasksByEpicId(HttpExchange exchange, int id) throws IOException {
        try {
            List<Task> subTasks = manager.getSubTasksByEpicId(id);
            if (subTasks.isEmpty()) {
                sendNotFound(exchange, "Tasks not found");
            }
            sendText(exchange, gson.toJson(subTasks), 200);
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, e.getMessage());
        }

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        try {
            RequestMethod requestMethod = getRequestMethod(path, exchange.getRequestMethod());

            switch (requestMethod) {
                case GET_TASKS -> getTasks(exchange);
                case GET_BY_ID -> getTask(exchange, getTaskId(path));
                case ADD_TASK -> addTask(exchange);
                case DELETE_BY_ID -> deleteTask(exchange, getTaskId(path));
                case GET_SUBTASKS_BY_EPIC_ID -> getSubTasksByEpicId(exchange, getTaskId(path));
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (MethodNotAllowedException e) {
            sendMethodNotFound(exchange, e.getMessage());
        }
    }


}
