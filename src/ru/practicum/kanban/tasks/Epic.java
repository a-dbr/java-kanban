package ru.practicum.kanban.tasks;

import ru.practicum.kanban.status.TaskStatus;

import java.util.ArrayList;
import java.util.List;


public class Epic extends Task {
    private boolean isDone = false;
    private final List<SubTask> subTasks;

    public Epic(String name, String description) {
        super(name, description);
        subTasks = new ArrayList<>();
    }

    public Epic(String name, String description, boolean isDone, TaskStatus taskStatus, List<SubTask> subTasks) {
        super(name, description, taskStatus);
        this.subTasks = subTasks;
        this.isDone = isDone;
    }

    public Epic(Epic epic) {
        this(epic.getName(), epic.getDescription(), epic.isDone(), epic.getTaskStatus(), epic.subTasks);
    }

    public List<SubTask> getSubTasks() {
        return subTasks;
    }

    public SubTask getSubTask(SubTask subTask) {
        int index = subTasks.indexOf(subTask);
        return subTasks.get(index);
    }


    public void addSubTask(SubTask subTask) {
        subTasks.add(subTask);
    }

    public void removeSubTask(Object o) {
        SubTask subTask = (SubTask) o;
        subTasks.remove(subTask);
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
                "name='" + this.getName() + '\'' +
                ", description='" + this.getDescription() + '\'' +
                ", taskID=" + this.getTaskID() +
                ", isDone=" + isDone +
                ", subTasks=" + subTasks +
                ", taskStatus=" + taskStatus +
                '}';
    }
}

