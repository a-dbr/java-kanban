package ru.practicum.kanban.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.Task;
import ru.practicum.kanban.model.enums.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class TaskManagerTest<T extends TaskManager>  {

    protected T taskManager;
    Task task;
    Task epic;

    protected abstract T initializeTaskManager();

    @BeforeEach
    void beforeEach() {
        taskManager = initializeTaskManager();
    }

     void addingTask() {
        Task sameTask = taskManager.getTaskById(task.getTaskId());

        assertEquals(1, taskManager.getAllTasks().size(),
                "The task wasn't added to the list.");

        Assertions.assertSame(task, sameTask,
                "Objects should be the same");
    }

    void getAllTasks() {
        epic = new Epic(
                "Epic",
                "Test epic description",
                LocalDateTime.of(2025, 1, 2, 0,0),
                Duration.ofHours(1)
        );

        taskManager.addTask(task);
        taskManager.addTask(epic);

        assertEquals(2, taskManager.getAllTasks().size(),
                "getAllTasks() returned an incorrect number of tasks");
    }

    void getTaskById() {
        int taskId = task.getTaskId();

        Task testTask1 = taskManager.getTaskById(taskId);
        Task testTask2 = taskManager.getTaskById(taskId);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                taskManager.getTaskById(-1));

        Assertions.assertSame(testTask1, testTask2,
                "The returned objects must be the same.");
        assertEquals("Task with ID -1 not found.", exception.getMessage());
    }

    void removeAllTasks() {
        epic = new Epic(
                "Epic",
                "Test epic description",
                LocalDateTime.of(2025, 1, 2, 0,0),
                Duration.ofHours(1)
        );
        taskManager.addTask(epic);
        taskManager.removeAllTasks();
        List<Task> taskList = taskManager.getAllTasks();

        Assertions.assertTrue(taskList.isEmpty(), "The list should be empty");
    }

    void removeTaskById() {
        int taskId = task.getTaskId();
        taskManager.removeTaskById(taskId);
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                taskManager.getTaskById(taskId));

        assertEquals("Task with ID " + taskId + " not found.", exception.getMessage());
    }

    void update() {
        int taskId = task.getTaskId();
        Task oldTask = taskManager.getTaskById(taskId);

        task = new Task(
                task.getName(),
                task.getDescription(),
                TaskStatus.IN_PROGRESS,
                task.getTaskId(),
                task.getStartTime(),
                task.getDuration()
        );
        taskManager.addTask(task);

        Task newTask = taskManager.getTaskById(taskId);

        Assertions.assertNotSame(newTask, oldTask, "The objects must be different");
    }
}
