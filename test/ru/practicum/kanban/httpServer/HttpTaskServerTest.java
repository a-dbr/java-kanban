package ru.practicum.kanban.httpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.httpServer.adapters.DurationAdapter;
import ru.practicum.kanban.httpServer.adapters.LocalDateTimeAdapter;
import ru.practicum.kanban.httpServer.adapters.RuntimeTypeAdapterFactory;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.SubTask;
import ru.practicum.kanban.model.Task;
import ru.practicum.kanban.model.enums.TaskStatus;
import ru.practicum.kanban.model.enums.TaskType;
import ru.practicum.kanban.service.Managers;
import ru.practicum.kanban.service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskServerTest {
    RuntimeTypeAdapterFactory<Task> taskAdapter = RuntimeTypeAdapterFactory
            .of(Task.class, "type")
            .registerSubtype(Task.class, TaskType.TASK.name())
            .registerSubtype(Epic.class, TaskType.EPIC.name())
            .registerSubtype(SubTask.class, TaskType.SUBTASK.name());

    protected Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapterFactory(taskAdapter)
            .setPrettyPrinting()
            .create();

    TaskManager manager;
    HttpTaskServer taskServer;
    Task task;
    Task epic;

    public HttpTaskServerTest() throws IOException {
        manager = Managers.getDefault();
        taskServer = new HttpTaskServer(manager);
    }

    @BeforeEach
    void beforeEach() {
        taskServer.start();
        task = new Task(
                "Task",
                "Test task description",
                LocalDateTime.of(2025, 1, 1, 0, 0),
                Duration.ofHours(1)
        );
    }

    @AfterEach
    void afterEach() {
        taskServer.stop();
    }

    @Test
    void addTask() throws IOException, InterruptedException {
        String taskToJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskToJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Expected HTTP 200 when adding a task");

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Tasks are not returned");
        assertEquals(1, tasksFromManager.size(), "Incorrect number of tasks");
        assertEquals("Task", tasksFromManager.get(0).getName(), "Incorrect task name");
    }

    @Test
    public void getTasks() throws IOException, InterruptedException {
        Task task2 = new Task(
                "Task2",
                "Test task2 description",
                LocalDateTime.of(2025, 1, 2, 0, 0),
                Duration.ofHours(1)
        );

        manager.addTask(task);
        manager.addTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(),
                "Expected HTTP 200 on getting all tasks");
        assertEquals(response.body(), gson.toJson(manager.getTasks()),
                "Response body should match manager.getTasks()");
    }

    @Test
    public void getEpics() throws IOException, InterruptedException {
        epic = new Epic(
                "Epic",
                "Test epic description",
                LocalDateTime.of(2025, 1, 1, 0, 0),
                Duration.ofHours(1)
        );
        Task epic2 = new Task(
                "Epic2",
                "Test epic2 description",
                LocalDateTime.of(2025, 1, 2, 0, 0),
                Duration.ofHours(1)
        );

        manager.addTask(epic);
        manager.addTask(epic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(),
                "Expected HTTP 200 on getting all epics");

        assertEquals(response.body(), gson.toJson(manager.getEpics()),
                "Response body should match manager.getEpics()");
    }

    @Test
    public void getSubTasks() throws IOException, InterruptedException {
        epic = new Epic(
                "Epic",
                "Test epic description",
                LocalDateTime.of(2025, 1, 1, 0, 0),
                Duration.ofHours(1)
        );
        SubTask subTask = new SubTask(
                "Subtask",
                "Test subtask description",
                epic.getTaskId(),
                LocalDateTime.of(2025, 1, 1, 0, 0),
                Duration.ofHours(1)
        );
        SubTask subTask2 = new SubTask(
                "Subtask2",
                "Test subtask2 description",
                epic.getTaskId(),
                LocalDateTime.of(2025, 1, 2, 0, 0),
                Duration.ofHours(1)
        );

        manager.addTask(epic);
        manager.addTask(subTask);
        manager.addTask(subTask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(),
                "Expected HTTP 200 on getting all subtasks");

        assertEquals(response.body(), gson.toJson(manager.getSubTasks()),
                "Response body should match manager.getSubTasks()");
    }

    @Test
    public void getSubTasksByEpicId() throws IOException, InterruptedException {
        epic = new Epic(
                "Epic",
                "Test epic description",
                LocalDateTime.of(2025, 1, 1, 0, 0),
                Duration.ofHours(1)
        );
        SubTask subTask = new SubTask(
                "Subtask",
                "Test subtask description",
                epic.getTaskId(),
                LocalDateTime.of(2025, 1, 1, 0, 0),
                Duration.ofHours(1)
        );
        SubTask subTask2 = new SubTask(
                "Subtask2",
                "Test subtask2 description",
                epic.getTaskId(),
                LocalDateTime.of(2025, 1, 2, 0, 0),
                Duration.ofHours(1)
        );

        manager.addTask(epic);
        manager.addTask(subTask);
        manager.addTask(subTask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getTaskId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected HTTP 200 on getting subtasks by epic ID");
        assertEquals(response.body(),
                gson.toJson(manager.getSubTasksByEpicId(epic.getTaskId())),
                "Response body should match manager.getSubTasksByEpicId()");
    }

    @Test
    void updateTask() throws IOException, InterruptedException {
        String taskToJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskToJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Expected HTTP 200 when adding a task");

        Task newTask = new Task(
                task.getName(),
                task.getDescription(),
                TaskStatus.IN_PROGRESS,
                task.getTaskId(),
                task.getStartTime(),
                task.getDuration()
        );

        String newTaskToJson = gson.toJson(newTask);

        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(newTaskToJson))
                .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Expected HTTP 200 when updating a task");

        List<Task> tasksFromManager = manager.getTasks();

        assertEquals(1, tasksFromManager.size(), "Incorrect number of tasks");
        assertEquals(TaskStatus.IN_PROGRESS,
                tasksFromManager.get(0).getTaskStatus(),
                "Incorrect task status");
    }

    @Test
    public void deleteTask() throws IOException, InterruptedException {
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getTaskId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Expected HTTP 200 when deleting a task");

        request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected HTTP 404 when fetching deleted task");
    }

    @Test
    void checkTasksTimeOverlap() throws IOException, InterruptedException {
        Task task2 = new Task(
                "Task2",
                "Test task2 description",
                LocalDateTime.of(2025, 1, 1, 0, 0),
                Duration.ofHours(1)
        );

        manager.addTask(task);
        String taskToJson = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskToJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Expected HTTP 406 when task time overlaps");
    }

    @Test
    void testInternalError() throws IOException, InterruptedException {
        String taskToJson = "{}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskToJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode(),"Expected HTTP 500 when sending an empty JSON");
    }
}
