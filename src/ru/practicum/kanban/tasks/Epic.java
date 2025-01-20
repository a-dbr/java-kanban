package ru.practicum.kanban.tasks;

import ru.practicum.kanban.status.TaskStatus;

import java.util.ArrayList;
import java.util.List;


public class Epic extends Task {
    private boolean isDone = false;
    private final List<Integer> subTasksIds;

    public Epic(String name, String description) {
        super(name, description);
        subTasksIds = new ArrayList<>();
    }

    public Epic(String name, String description, boolean isDone, TaskStatus taskStatus, int taskId, List<Integer> subTasksIds) {
        super(name, description, taskStatus, taskId);
        this.subTasksIds = subTasksIds;
        this.isDone = isDone;
    }

    public Epic(Epic epic) {
        this(epic.getName(), epic.getDescription(), epic.isDone(), epic.getTaskStatus(), epic.getTaskId(), epic.subTasksIds);
    }

    public List<Integer> getSubTasksIds() {
        return subTasksIds;
    }

    public int getSubTaskId(int subTaskId) {
        int index = subTasksIds.indexOf(subTaskId);
        return subTasksIds.get(index);
    }

    public void addSubTaskId(int subTaskId) {
        subTasksIds.add(subTaskId);
    }

    public void removeSubTaskById(Integer subTaskId) {
        subTasksIds.remove(subTaskId);
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + this.getDescription() + '\'' +
                ", taskId=" + this.getTaskId() +
                ", isDone=" + isDone +
                ", subTasksIds=" + subTasksIds +
                ", taskStatus=" + taskStatus +
                '}';
    }
}