package ru.practicum.kanban.service;

import ru.practicum.kanban.model.Task;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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

class FileBackedTaskManagerTest {
    TaskManager taskManager;
    static final Path tempDir = Paths.get("./test/temp");
    Path tempFilePath;

    @BeforeEach
    void beforeEach() throws IOException {
        String simulatedInput = "y\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);

        if (Files.notExists(tempDir)) {
            Files.createDirectory(tempDir);
        }
        tempFilePath = Files.createTempFile(tempDir, "file-", ".tmp");
        taskManager = Managers.getDefault(tempFilePath);
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
        Assertions.assertTrue(((FileBackedTaskManager) taskManager).exceptionCaught);
    }
    @Test
    void addTask() throws IOException {
        Task task = new Task("Task", "Test task description");
        Task task2 = new Task("Task2", "Test task description2");
        taskManager.addTask(task);
        taskManager.addTask(task2);

        List<String> test = Arrays.asList("id,type,name,status,description,epic",
                "1,TASK,Task,NEW,Test task description",
                "2,TASK,Task2,NEW,Test task description2");

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