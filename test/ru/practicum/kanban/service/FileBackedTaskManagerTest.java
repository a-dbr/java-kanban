package ru.practicum.kanban.service;

import ru.practicum.kanban.model.Task;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    static final Path tempDir = Paths.get("./test/temp");
    Path tempFilePath;

    @Override
    protected FileBackedTaskManager initializeTaskManager() {
        return new FileBackedTaskManager(tempFilePath);
    }

    @BeforeEach
    void beforeEach() {
        try {
            String simulatedInput = "y\n";

            InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(in);

            if (Files.notExists(tempDir)) {
                Files.createDirectory(tempDir);
            }
            tempFilePath = Files.createTempFile(tempDir, "file-", ".tmp");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        super.beforeEach();
    }

    @AfterEach
    void afterEach() throws IOException {
        Files.delete(tempFilePath);
    }

    @AfterAll
    static void afterAll() throws IOException {
        try {
            Files.delete(tempDir);
        } catch (DirectoryNotEmptyException e) {
            deleteDirectoryRecursively(tempDir);
        } catch (FileAlreadyExistsException e) {
            System.out.println("Please restart the test");
        }
    }
    @Test
    void loadEmptyFile() {
        Assertions.assertTrue(taskManager.exceptionCaught);
    }
    @Test
    void addTask() throws IOException {
        Task task = new Task(
                "Task",
                "Test task description",
                LocalDateTime.of(2025, 1, 1, 0,0),
                Duration.ofHours(1)
        );
        Task task2 = new Task(
                "Task2",
                "Test task description2",
                LocalDateTime.of(2025, 1, 2, 0,0),
                Duration.ofHours(1)
        );
        taskManager.addTask(task);
        taskManager.addTask(task2);

        List<String> test = Arrays.asList("id,type,name,status,description,startTime,duration,epicStartTime,epicDuration,epicId",
                "1,TASK,Task,NEW,Test task description,2025-01-01T00:00,PT1H",
                "2,TASK,Task2,NEW,Test task description2,2025-01-02T00:00,PT1H");

        assertEquals(test, Files.readAllLines(tempFilePath));
    }

    public static void deleteDirectoryRecursively(Path path) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    deleteDirectoryRecursively(entry);
                } else {
                    Files.delete(entry);
                }
            }
        }
        Files.delete(path);
    }
}