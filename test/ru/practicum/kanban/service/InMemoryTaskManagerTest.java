package ru.practicum.kanban.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.exceptions.TaskIsOverlapException;
import ru.practicum.kanban.model.Epic;
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
        Task subTask = new SubTask(
                "Name",
                "Description",
                9999,
                LocalDateTime.of(2025, 1, 2, 0,0),
                Duration.ofHours(1)
        );

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            taskManager.addTask(subTask);
        });
        assertEquals("Unable to add subtask: an epic with this ID does not exist.", exception.getMessage());
    }

    @Test
    void checkSubTasksTimeOverlap() {
        Task epic = new Epic(
                "Epic",
                "",
                LocalDateTime.of(1970, 1,1,0,0),
                Duration.ofHours(24)
        );
        SubTask firstSubTask = new SubTask(
                "FirstSubTask",
                "",
                epic.getTaskId(),
                LocalDateTime.of(1970,1,1,0,0),
                Duration.ofHours(24)
        );

        SubTask secondSubTask = new SubTask(
                "SecondSubTask",
                "",
                epic.getTaskId(),
                LocalDateTime.of(1970,1,1,0,0),
                Duration.ofHours(24)
        );

        TaskIsOverlapException exception = assertThrows(TaskIsOverlapException.class, () -> {
            taskManager.addTask(epic);
            taskManager.addTask(firstSubTask);
            taskManager.addTask(secondSubTask);
        });
        assertEquals("The added task SecondSubTask overlaps the existing task!", exception.getMessage());
    }

    @Test
    void checkTimeOverlapBetweenTaskAndEpic() {
        Task epic = new Epic(
                "Epic",
                "",
                LocalDateTime.of(2025, 1, 1, 0,0),
                Duration.ofHours(24)
        );

        TaskIsOverlapException exception = assertThrows(TaskIsOverlapException.class, () -> {
            taskManager.addTask(epic);
        });
        assertEquals("The added task Epic overlaps the existing task!", exception.getMessage());
    }
}