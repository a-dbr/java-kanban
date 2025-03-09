package ru.practicum.kanban.service;

import org.junit.jupiter.api.*;
import ru.practicum.kanban.model.Task;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class FileBackedTaskManagerTest {
    TaskManager taskManager;
    static final Path tempDir = Path.of("./test/temp");
    Path tempFilePath;

    @BeforeEach
    void beforeEach() throws IOException {
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
        assertTrue(taskManager.getAllTasks().isEmpty());
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