package ru.practicum.kanban.tasks;

import ru.practicum.kanban.service.TaskManager;
import ru.practicum.kanban.status.TaskStatus;

import java.util.Objects;

public class Task {
    private String name;
    private String description;
    TaskStatus taskStatus;
    private int taskId;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        updateStatus(taskStatus);
        if (taskId == 0) {
            this.taskId = TaskManager.generateTaskId();
        }
    }

    public Task(String name, String description, TaskStatus taskStatus, int taskId) {
        this.name = name;
        this.description = description;
        this.taskId = taskId;
        updateStatus(taskStatus);
    }

    public Task(String name, String description, int taskId) {
        this(name, description);
        this.taskId = taskId;
    }

    public Task(Task task) {
        this(task.name, task.description, task.taskId);
        updateStatus(task.getTaskStatus());
    }

    void updateStatus(TaskStatus taskStatus) {
        switch (taskStatus) {
            case null:
                this.taskStatus = TaskStatus.NEW;
                break;
            case NEW:
                this.taskStatus = TaskStatus.IN_PROGRESS;
                break;
            case IN_PROGRESS:
                this.taskStatus = TaskStatus.DONE;
                break;
            case DONE:
                break;
        }
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

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
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