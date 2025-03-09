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
import java.util.List;
import java.util.NoSuchElementException;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final String taskIdCounterFileName = "./src/ru/practicum/kanban/resources/task_id_counter.csv";
    private final Path taskCounterFile = Paths.get(taskIdCounterFileName);
    private final Path file;

    public FileBackedTaskManager(Path file) {
        this.file = file;
        try {
            if (Files.exists(file) && Files.size(file) != 0) {
                loadFromFile();
                taskCounter = loadTaskCounterFromFile();
            }
        } catch (IOException e) {
            throw new ManagerLoadException("File reading error: " + e.getMessage());
        }
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
        saveTaskCounter();
    }

    private Task fromString(String value) {
        String[] data = value.split(",", 6);
        try {
            int taskId = Integer.parseInt(data[0]);
            TaskType taskType = TaskType.valueOf(data[1]);
            String taskName = data[2];
            TaskStatus taskStatus = TaskStatus.valueOf(data[3]);
            String taskDescription = data[4];

            return switch (taskType) {
                case TASK -> new Task(taskName, taskDescription, taskStatus, taskId);
                case EPIC -> new Epic(taskName, taskDescription, taskStatus, taskId);
                case SUBTASK -> {
                    int epicId = Integer.parseInt(data[5]);
                    yield new SubTask(taskName, taskDescription, taskStatus, taskId, epicId);
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

    private void loadFromFile() throws IOException {
        // Ревьюеру: если бы работал с sql, сделал бы загрузку напрямую в мапы.

        final List<String> data = Files.readAllLines(file);
        for (int i = 1; i < data.size(); i++) { // first line is header
            try {
                Task task = fromString(data.get(i));
                super.addTask(task);
            } catch (NoSuchElementException e) {
                System.out.println(e.getMessage());
            }
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
            writer.write(TaskCsvFormatHandler.getHeader());
            writer.newLine();

            List<Task> allTasks = getAllTasks();
            for (Task task : allTasks) {
                writer.write(task.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error: " + e.getMessage());
        }
    }

    private void saveTaskCounter() throws ManagerSaveException {
        try {
            Files.writeString(taskCounterFile, ((Integer) taskCounter).toString());
        } catch (IOException e) {
            throw new ManagerLoadException("Error writing task counter file: " + e.getMessage());
        }
    }

    @Override
    public void update(Task task) {
        super.update(task);
    }
}
