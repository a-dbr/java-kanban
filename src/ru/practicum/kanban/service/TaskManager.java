package ru.practicum.kanban.service;

import ru.practicum.kanban.model.Task;

import java.util.List;

public interface TaskManager {
    static int generateTaskId() {
        return ++InMemoryTaskManager.taskCounter;
    }

    void addTask(Task task);

    List<Task> getAllTasks();

    List<Task> getHistory();

    Task getTaskById(int taskId);

    void removeAllTasks();

    void removeTaskById(int taskId);

    void update(Task task);
}
