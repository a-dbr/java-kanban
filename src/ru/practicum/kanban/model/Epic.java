package ru.practicum.kanban.model;

import ru.practicum.kanban.model.enums.TaskStatus;
import ru.practicum.kanban.model.enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Epic extends Task {
    private List<Integer> subTasksIds = new ArrayList<>();
    private LocalDateTime epicStartTime;
    private Duration epicDuration;

    public Epic(String name, String description, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        epicStartTime = startTime;
        epicDuration = duration;
    }

    public Epic(String name, String description, TaskStatus taskStatus, int taskId, LocalDateTime startTime,
                Duration duration) {
        super(name, description, taskStatus, taskId, startTime, duration);
    }

    public Epic(String name, String description, TaskStatus taskStatus, int taskId, List<Integer> subTasksIds,
                LocalDateTime startTime, Duration duration, LocalDateTime epicStartTime, Duration epicDuration) {

        this(name, description, taskStatus, taskId, startTime, duration);
        this.subTasksIds = subTasksIds;
        this.epicStartTime = epicStartTime;
        this.epicDuration = epicDuration;
    }

    public Epic(Epic epic) {
        this(epic.getName(), epic.getDescription(), epic.getTaskStatus(), epic.getTaskId(), epic.subTasksIds,
                epic.getStartTime(), epic.getDuration(), epic.epicStartTime, epic.epicDuration);
    }

    public Epic(Epic epic, TaskStatus taskStatus) {
        this(epic.getName(), epic.getDescription(), taskStatus, epic.getTaskId(), epic.subTasksIds, epic.getStartTime(),
                epic.getDuration(), epic.epicStartTime, epic.epicDuration);
    }

    public Epic(Epic epic, LocalDateTime startTime, Duration duration, LocalDateTime epicStartTime,
                Duration epicDuration) {
        this(epic.getName(), epic.getDescription(), epic.getTaskStatus(), epic.getTaskId(), epic.subTasksIds, startTime,
                duration, epicStartTime, epicDuration);
    }

    public Duration getEpicDuration() {
        return epicDuration;
    }

    public LocalDateTime getEpicStartTime() {
        return epicStartTime;
    }

    public List<Integer> getSubTasksIds() {
        return subTasksIds;
    }

    public void addSubTaskId(int subTaskId) {
        subTasksIds.add(subTaskId);
    }

    public void removeSubTaskById(Integer subTaskId) {
        subTasksIds.remove(subTaskId);
    }

    @Override
    public String toString() {
        return getTaskId() + "," +
                TaskType.EPIC + "," +
                getName() + "," +
                getTaskStatus() + "," +
                getDescription()  + "," +
                getStartTime()  + "," +
                getDuration()  + "," +
                epicStartTime  + "," +
                epicDuration;
    }
}