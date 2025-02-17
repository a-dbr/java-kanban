package ru.practicum.kanban.service;

import ru.practicum.kanban.model.Task;

import java.util.List;

public interface HistoryManager {
    void addTask(Task task);

    List<Task> getHistory();

    void remove(int id);
}
