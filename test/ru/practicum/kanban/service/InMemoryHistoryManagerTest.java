package ru.practicum.kanban.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.model.Task;
import ru.practicum.kanban.model.enums.TaskStatus;

import java.util.List;

class InMemoryHistoryManagerTest {

    TaskManager taskManager;
    Task task;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
    }

    @Test
    void addTask() {
        task = new Task("Task", "Test task description");
        taskManager.addTask(task);
        taskManager.getTaskById(task.getTaskId());

        Assertions.assertTrue(taskManager.getHistory().contains(task));
    }

    @Test
    void removeTask() {
        task = new Task("Task", "Test task description");
        taskManager.addTask(task);
        taskManager.getTaskById(task.getTaskId());
        taskManager.removeTaskById(task.getTaskId());

        Assertions.assertFalse(taskManager.getHistory().contains(task));
    }

    @Test
    void removeAllTasks() {
        for (int i = 0; i < 20; i++) {
            task = new Task("Task", String.valueOf(i));
            taskManager.addTask(task);
            taskManager.getTaskById(task.getTaskId());
        }
        taskManager.removeAllTasks();

        int size = taskManager.getHistory().size();
        Assertions.assertEquals(0, size);
    }

    @Test
    void shouldNotDeleteTheOldestTaskWhenThereAreMore10Tasks() {
        for (int i = 0; i < 20; i++) {
            task = new Task("Task", String.valueOf(i));
            taskManager.addTask(task);
            taskManager.getTaskById(task.getTaskId());
        }
        int size = taskManager.getHistory().size();
        Assertions.assertEquals(20, size, "The values in the history manager don't match");
    }

    @Test
    void shouldNotReturnDuplicateBrowsingHistories() {
        task = new Task("Task", "Test task description");
        taskManager.addTask(task);
        task = taskManager.getTaskById(task.getTaskId());
        task = new Task(task.getName(), task.getDescription(), TaskStatus.IN_PROGRESS, task.getTaskId());
        taskManager.addTask(task);
        task = taskManager.getTaskById(task.getTaskId());
        List<Task> history = taskManager.getHistory();

        Assertions.assertEquals(1, history.size(),
                "The returned number of requests doesn't match the expected number.");
    }
}