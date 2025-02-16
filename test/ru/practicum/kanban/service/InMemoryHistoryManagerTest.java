package ru.practicum.kanban.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.model.Task;
import ru.practicum.kanban.status.TaskStatus;

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

        Assertions.assertEquals(1, taskManager.getHistory().size());
    }

    @Test
    void shouldDeleteTheOldestTaskWhenThereAreMore10Tasks() {
        Task task;
        for (int i = 0; i < 11; i++) {
            task = new Task("Task", String.valueOf(i));
            taskManager.addTask(task);
            taskManager.getTaskById(task.getTaskId());
        }
        task = taskManager.getHistory().getFirst();
        String description = task.getDescription();

        Assertions.assertEquals("1", description, "The values in the history manager don't match");
    }

    @Test
    void getHistory() {
        task = new Task("Task", "Test task description");
        taskManager.addTask(task);
        task = taskManager.getTaskById(task.getTaskId());
        task = new Task(task.getName(), task.getDescription(), TaskStatus.IN_PROGRESS, task.getTaskId());
        taskManager.addTask(task);
        task = taskManager.getTaskById(task.getTaskId());
        List<Task> history = taskManager.getHistory();

        Assertions.assertEquals(2, history.size(),
                "The returned number of requests doesn't match the expected number.");

        Assertions.assertNotSame(history.get(0), history.get(1),
                "Returned tasks should be different");
    }
}