package ru.practicum.kanban.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.model.SubTask;
import ru.practicum.kanban.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager>{

    @Override
    protected InMemoryTaskManager initializeTaskManager() {
        return new InMemoryTaskManager();
    }

    @BeforeEach
    void beforeEach() {
        super.beforeEach();
        task = new Task("Task",
                "Test task description",
                LocalDateTime.of(2025, 1, 1, 0,0),
                Duration.ofHours(1)
        );
        taskManager.addTask(task);
    }

    @Test
    void addingTask() {
        super.addingTask();
    }

    @Test
    void getAllTasks() {
        super.getAllTasks();
    }

    @Test
    void getTaskById() {
        super.getTaskById();
    }

    @Test
    void removeAllTasks() {
        super.removeAllTasks();
    }

    @Test
    void removeTaskById() {
        super.removeTaskById();
    }

    @Test
    void update() {
        super.update();
    }

    @Test
    void checkAddingSubtaskWithTaskId() {
        System.out.println(taskManager.getPrioritizedTasks());
        System.out.println(taskManager.getAllTasks());
        Task subTask = new SubTask(
                "Name",
                "Description",
                9999,
                LocalDateTime.of(2025, 1, 2, 0,0),
                Duration.ofHours(1)
        );
        System.out.println(subTask);
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            taskManager.addTask(subTask);
        });
        assertEquals("Unable to add subtask: an epic with this ID does not exist.", exception.getMessage());
    }
}