package ru.practicum.kanban.service;

import ru.practicum.kanban.exceptions.ManagerLoadException;
import ru.practicum.kanban.exceptions.ManagerSaveException;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.SubTask;
import ru.practicum.kanban.model.Task;
import ru.practicum.kanban.model.enums.TaskStatus;
import ru.practicum.kanban.model.enums.TaskType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final String taskIdCounterFileName = "./src/ru/practicum/kanban/resources/task_id_counter.csv";
    private final Path taskCounterFile = Paths.get(taskIdCounterFileName);
    private final Path file;
    boolean exceptionCaught = false;

    public FileBackedTaskManager(Path file) {
        this.file = file;
        try {
            Path resourcesDir = Paths.get("./src/ru/practicum/kanban/resources/");
            if (Files.notExists(resourcesDir)) {
                Files.createDirectory(resourcesDir);
            }

            if (Files.exists(file)) {
                loadFromFile();
                taskCounter = loadTaskCounterFromFile();
            }
        } catch (IOException e) {
            throw new ManagerLoadException("File reading error: " + e.getMessage());
        } catch (ManagerLoadException e) {
            exceptionCaught = true;
            Scanner scanner = new Scanner(System.in);
            System.out.println(e.getMessage() + "\n");
            while (true) {
                System.out.println("Continue and overwrite the file?");
                System.out.println("Type 'y/n':");
                String input = scanner.nextLine();
                switch (input) {
                    case "y":
                        return;
                    case "n":
                        System.exit(1);
                    default:
                        System.out.println("Incorrect input");
                }
            }
        }
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
        saveTaskCounter();
    }

    private Task fromString(String value) {
        String[] data = value.split(",", 10);
        try {
            int taskId = Integer.parseInt(data[0]);
            TaskType taskType = TaskType.valueOf(data[1]);
            String taskName = data[2];
            TaskStatus taskStatus = TaskStatus.valueOf(data[3]);
            String taskDescription = data[4];
            String startTime = data[5];
            String duration = data[6];

            return switch (taskType) {
                case TASK -> new Task(
                        taskName,
                        taskDescription,
                        taskStatus,
                        taskId,
                        LocalDateTime.parse(startTime),
                        Duration.parse(duration)
                );
                case EPIC -> {
                    String epicStartTime = data[7];
                    String epicDuration = data[8];
                    yield new Epic(
                            taskName,
                            taskDescription,
                            taskStatus,
                            taskId,
                            new ArrayList<>(),
                            LocalDateTime.parse(startTime),
                            Duration.parse(duration),
                            LocalDateTime.parse(epicStartTime),
                            Duration.parse(epicDuration)
                    );
                }
                case SUBTASK -> {
                    int epicId = Integer.parseInt(data[9]);
                    yield new SubTask(
                            taskName,
                            taskDescription,
                            taskStatus,
                            taskId,
                            epicId,
                            LocalDateTime.parse(startTime),
                            Duration.parse(duration)
                    );
                }
            };
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Error loading data from file: " + e.getMessage());
            System.exit(1);
        } catch (NumberFormatException e) {
            System.out.println("There is no task data in the string: " + e.getMessage());
        }
        throw new NoSuchElementException("Error loading data from string.");
    }

    private void loadFromFile() throws IOException, ManagerSaveException {
        if (Files.size(file) != 0) {
            final List<String> data = Files.readAllLines(file);
            for (int i = 1; i < data.size(); i++) { // first line is header
                try {
                    Task task = fromString(data.get(i));
                    super.addTask(task);
                } catch (NoSuchElementException e) {
                    System.out.println(e.getMessage());
                }
            }
        } else {
            throw new ManagerLoadException("File " + file + " is empty");
        }
    }

    private int loadTaskCounterFromFile() {
        try {
            if (Files.exists(taskCounterFile)) {
                return Integer.parseInt(Files.readString(taskCounterFile));
            } else {
                Files.createFile(taskCounterFile);
                return 0;
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Error reading task counter file: " + e.getMessage());
        }
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
        saveTaskCounter();
    }

    @Override
    public void removeTaskById(int taskId) {
        super.removeTaskById(taskId);
        save();
    }

    private void save() throws ManagerSaveException {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(TaskCsvFormatHandler.getHeader()); // Write CSV header
            writer.newLine();

            List<Task> allTasks = getAllTasks();
            for (Task task : allTasks) {
                writer.write(task.getDataForFileSaving());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error: " + e.getMessage());
        }
    }

    private void saveTaskCounter() {
        try {
            if (Files.notExists(taskCounterFile)) {
                Files.createFile(taskCounterFile);
            }
            Files.writeString(taskCounterFile, ((Integer) taskCounter).toString());
        } catch (IOException e) {
            throw new ManagerSaveException("Error writing task counter file: " + e.getMessage());
        }
    }

    @Override
    public void update(Task task) {
        super.update(task);
        save();
    }
}