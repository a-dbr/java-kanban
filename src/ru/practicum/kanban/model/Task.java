package ru.practicum.kanban.model;

import ru.practicum.kanban.service.TaskManager;
import ru.practicum.kanban.status.TaskStatus;

import java.util.Objects;

public class Task {
    private final String name;
    private final String description;
    private final TaskStatus taskStatus;
    private final int taskId;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.taskStatus = TaskStatus.NEW;
        this.taskId = TaskManager.generateTaskId();
    }

    public Task(String name, String description, TaskStatus taskStatus, int taskId) {
        this.name = name;
        this.description = description;
        this.taskId = taskId;
        this.taskStatus = taskStatus;
    }

    public Task(Task task) {
        this(task.name, task.description, task.taskStatus, task.taskId);
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public int getTaskId() {
        return taskId;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", taskStatus=" + taskStatus +
                ", taskId=" + taskId +
                '}';
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