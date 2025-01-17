package ru.practicum.kanban.tasks;

import ru.practicum.kanban.status.TaskStatus;

import java.util.Objects;

public class Task {
    private final String name;
    private final String description;
    TaskStatus taskStatus;
    private int taskID;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        taskID = this.hashCode();
        updateStatus(taskStatus);
    }

    public Task(String name, String description, TaskStatus taskStatus) {
        this.name = name;
        this.description = description;
        taskID = this.hashCode();
        updateStatus(taskStatus);
    }

    public Task(String name, String description, int taskID) {
        this(name, description);
        this.taskID = taskID;
    }

    public Task(Task task) {
        this(task.name, task.description, task.taskID);
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

    public int getTaskID() {
        return taskID;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", taskStatus=" + taskStatus +
                ", taskID=" + taskID +
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
