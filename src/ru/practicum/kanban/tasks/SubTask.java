package ru.practicum.kanban.tasks;

public class SubTask extends Task {
    private final int epicID;

    public SubTask(String name, String description, int epicID) {
        super(name, description);
        this.epicID = epicID;
    }

    public SubTask(SubTask subTask, int epicID) {
        super(subTask);
        this.epicID = epicID;
    }

    public int getEpicID() {
        return epicID;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "name='" + this.getName() + '\'' +
                ", description='" + this.getDescription() + '\'' +
                ", taskID=" + this.getTaskID() +
                ", epicID=" + epicID +
                ", taskStatus=" + taskStatus +
                '}';
    }
}
