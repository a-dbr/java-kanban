package ru.practicum.kanban.model;

import ru.practicum.kanban.model.enums.TaskType;
import ru.practicum.kanban.service.TaskManager;
import ru.practicum.kanban.model.enums.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task implements Comparable<Task> {
    private final String name;
    private final String description;
    private final Duration duration;
    private final LocalDateTime startTime;
    private final TaskStatus taskStatus;
    private final int taskId;

    public Task(String name, String description, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.taskStatus = TaskStatus.NEW;
        this.taskId = TaskManager.generateTaskId();
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String name, String description, TaskStatus taskStatus, int taskId, LocalDateTime startTime,
                Duration duration) {
        this.name = name;
        this.description = description;
        this.taskId = taskId;
        this.taskStatus = taskStatus;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(Task task) {
        this(task.name, task.description, task.taskStatus, task.taskId, task.startTime, task.duration);
    }

    public String getDataForFileSaving() {
        return this.toString();
    }

    public String getDescription() {
        return description;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public int getTaskId() {
        return taskId;
    }

    @Override
    public int compareTo(Task task) {
        return this.startTime.compareTo(task.startTime);
    }

    public LocalDateTime getEndTime() {
        if (startTime != null && duration != null) {
            return startTime.plus(duration);
        }
        return null;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public TaskType getTaskType() {
        if (this.getClass() == Task.class) {
            return TaskType.TASK;
        } else if (this instanceof Epic) {
            return TaskType.EPIC;
        } else if (this instanceof SubTask) {
            return TaskType.SUBTASK;
        }
        throw new IllegalStateException("Unknown Task subtype: " + getClass());
    }

    @Override
    public String toString() {
        return taskId + "," +
                getTaskType() + "," +
                name + "," +
                taskStatus + "," +
                description  + "," +
                startTime  + "," +
                duration;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(name, task.name) &&
                Objects.equals(description, task.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, taskStatus);
    }
}