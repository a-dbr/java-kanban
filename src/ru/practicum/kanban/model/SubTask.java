package ru.practicum.kanban.model;

import ru.practicum.kanban.model.enums.TaskStatus;
import ru.practicum.kanban.model.enums.TaskType;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public SubTask(String name, String description, TaskStatus taskStatus, int taskId, int epicId) {
        super(name, description, taskStatus, taskId);
        this.epicId = epicId;
    }

    public SubTask(SubTask subTask) {
        this(
                subTask.getName(),
                subTask.getDescription(),
                subTask.getTaskStatus(),
                subTask.getTaskId(),
                subTask.getEpicId()
        );
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return getTaskId() + "," +
                TaskType.SUBTASK + "," +
                getName() + "," +
                getTaskStatus() + "," +
                getDescription() + "," +
                epicId;
    }
}