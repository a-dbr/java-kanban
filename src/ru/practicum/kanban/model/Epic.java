package ru.practicum.kanban.model;

import ru.practicum.kanban.status.TaskStatus;

import java.util.ArrayList;
import java.util.List;


public class Epic extends Task {
    private List<Integer> subTasksIds;

    public Epic(String name, String description) {
        super(name, description);
        subTasksIds = new ArrayList<>();
    }

    public Epic(
            String name,
            String description,
            TaskStatus taskStatus,
            int taskId) {

        super(name, description, taskStatus, taskId);
    }

    public Epic(
            String name,
            String description,
            TaskStatus taskStatus,
            int taskId,
            List<Integer> subTasksIds) {

        this(name, description, taskStatus, taskId);


        this.subTasksIds = subTasksIds;
    }

    public Epic(Epic epic) {
        this(
                epic.getName(),
                epic.getDescription(),
                epic.getTaskStatus(),
                epic.getTaskId(),
                epic.subTasksIds);
    }

    public Epic(Epic epic, TaskStatus taskStatus) {
        this(
                epic.getName(),
                epic.getDescription(),
                taskStatus,
                epic.getTaskId(),
                epic.subTasksIds);
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
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + this.getDescription() + '\'' +
                ", taskId=" + this.getTaskId() +
                ", subTasksIds=" + subTasksIds +
                ", taskStatus=" + this.getTaskStatus() +
                '}';
    }
}