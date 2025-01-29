package ru.practicum.kanban.service;

import ru.practicum.kanban.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final static int SIZE_OF_HISTORY = 10;
    private final List<Task> history = new ArrayList<>();

    @Override
    public void addTask(Task task) {
        if (history.size() >= SIZE_OF_HISTORY) {
            history.removeFirst();
        }

        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
