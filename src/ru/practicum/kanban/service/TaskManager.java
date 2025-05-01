package ru.practicum.kanban.service;

import ru.practicum.kanban.model.Task;

import java.util.List;
import java.util.Set;

public interface TaskManager {
    static int generateTaskId() {
        return ++InMemoryTaskManager.taskCounter;
    }

    void addTask(Task task);

    List<Task> getAllTasks();

    List<Task> getEpics();

    List<Task> getHistory();

    Set<Task> getPrioritizedTasks();

    List<Task> getSubTasks();

    List<Task> getSubTasksByEpicId(int epicId);

    Task getTaskById(int taskId);

    List<Task> getTasks();

    boolean isExist(Task task);

    void removeAllTasks();

    void removeTaskById(int taskId);

    void update(Task task);
}
