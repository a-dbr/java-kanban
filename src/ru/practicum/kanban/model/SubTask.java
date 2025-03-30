package ru.practicum.kanban.model;

import ru.practicum.kanban.model.enums.TaskStatus;
import ru.practicum.kanban.model.enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(String name, String description, int epicId, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.epicId = epicId;
    }

    public SubTask(
            String name,
            String description,
            TaskStatus taskStatus,
            int taskId,
            int epicId,
            LocalDateTime startTime,
            Duration duration) {
        super(name, description, taskStatus, taskId, startTime, duration);
        this.epicId = epicId;
    }

    public SubTask(SubTask subTask) {
        this(
                subTask.getName(),
                subTask.getDescription(),
                subTask.getTaskStatus(),
                subTask.getTaskId(),
                subTask.getEpicId(),
                subTask.getStartTime(),
                subTask.getDuration());
    }

    @Override
    public String getDataForFileSaving() {
        return getTaskId() + "," +
                TaskType.SUBTASK + "," +
                getName() + "," +
                getTaskStatus() + "," +
                getDescription()  + "," +
                getStartTime()  + "," +
                getDuration() + "," +
                "," + // for epicStartTime
                "," + // for epicDuration
                epicId;
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
                getDescription()  + "," +
                getStartTime()  + "," +
                getDuration() + "," +
                epicId;
    }
}