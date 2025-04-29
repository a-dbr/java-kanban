package ru.practicum.kanban.httpServer;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.kanban.httpServer.handlers.HistoryHandler;
import ru.practicum.kanban.httpServer.handlers.PrioritizedTaskHandler;
import ru.practicum.kanban.httpServer.handlers.TaskHandler;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.SubTask;
import ru.practicum.kanban.model.Task;
import ru.practicum.kanban.model.enums.TaskType;
import ru.practicum.kanban.service.Managers;
import ru.practicum.kanban.service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer httpServer;

    public HttpTaskServer(TaskManager manager) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT),0);
        httpServer.createContext("/tasks", new TaskHandler(manager, TaskType.TASK));
        httpServer.createContext("/epics", new TaskHandler(manager, TaskType.EPIC));
        httpServer.createContext("/subtasks", new TaskHandler(manager, TaskType.SUBTASK));
        httpServer.createContext("/prioritized", new PrioritizedTaskHandler(manager));
        httpServer.createContext("/history", new HistoryHandler(manager));
    }

    public static void main(String[] args) throws Exception {
        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task(
                "Task name",
                "Task description",
                LocalDateTime.of(2025, 1, 1, 0,0),
                Duration.ofHours(1));
        taskManager.addTask(task1);


        Task task2 = new Epic(
                "Task2 name",
                "Task2 description",
                LocalDateTime.of(2025, 1, 5, 0,0),
                Duration.ofHours(1));

        Task subTask1 = new SubTask(
                "Subtask1",
                "Subtask1 description",
                task2.getTaskId(),
                LocalDateTime.of(2025, 2, 5, 0,0),
                Duration.ofHours(8));
        Task subTask2 = new SubTask(
                "Subtask2",
                "Subtask2 description",
                task2.getTaskId(),
                LocalDateTime.of(2025, 3, 5, 0,0),
                Duration.ofHours(8));

        taskManager.addTask(task2);
        taskManager.addTask(subTask1);
        taskManager.addTask(subTask2);

        HttpTaskServer server = new HttpTaskServer(taskManager);
        server.start();
        System.out.println(taskManager.getAllTasks());
    }

    public void start() {
        httpServer.start();
        System.out.println("Server started on port " + PORT);
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("Server stopped");
    }
}
