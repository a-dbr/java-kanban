package ru.practicum.kanban.httpServer.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.kanban.exceptions.MethodNotAllowedException;
import ru.practicum.kanban.exceptions.NotFoundException;
import ru.practicum.kanban.httpServer.RequestMethod;
import ru.practicum.kanban.httpServer.adapters.DurationAdapter;
import ru.practicum.kanban.httpServer.adapters.LocalDateTimeAdapter;
import ru.practicum.kanban.model.Task;
import ru.practicum.kanban.service.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import static ru.practicum.kanban.httpServer.RequestMethod.GET_TASKS;

public class PrioritizedTaskHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    protected final Gson gson;

    public PrioritizedTaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .setPrettyPrinting()
                .create();
    }

    private RequestMethod getRequestMethod(String path, String requestMethod)
            throws NotFoundException, MethodNotAllowedException {
        String[] segments = path.split("/");
        if (segments.length <= 2 && "prioritized".equals(segments[1])) {
            if ("GET".equals(requestMethod)) {
                return GET_TASKS;
            }
            throw new MethodNotAllowedException("Method " + requestMethod + " not supported for " + path);
        }
        throw new NotFoundException("URI not recognized: " + path);
    }

    private void getHistory(HttpExchange exchange) throws IOException {
        Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        if (prioritizedTasks.isEmpty()) {
            sendNotFound(exchange, "Prioritized task set is empty");
        }
        sendText(exchange, gson.toJson(prioritizedTasks), 200);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            RequestMethod requestMethod = getRequestMethod(
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestMethod()
            );
            if (requestMethod == GET_TASKS) {
                getHistory(exchange);
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (MethodNotAllowedException e) {
            sendMethodNotFound(exchange, e.getMessage());
        }
    }
}
