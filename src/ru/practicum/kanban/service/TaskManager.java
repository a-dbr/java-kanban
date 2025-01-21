package ru.practicum.kanban.service;

import ru.practicum.kanban.status.TaskStatus;
import ru.practicum.kanban.tasks.Epic;
import ru.practicum.kanban.tasks.SubTask;
import ru.practicum.kanban.tasks.Task;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TaskManager {
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, SubTask> subTasks;
    public static int taskCounter;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();

        taskCounter = 0;
    }

    public void addTask(Task task) {
        if (task.getClass().equals(Task.class)) {
            tasks.put(task.getTaskId(), task);

        } else if (task.getClass().equals(Epic.class)) {
            epics.put(task.getTaskId(), (Epic) task);

        } else if (task.getClass().equals(SubTask.class)) {
            int epicId = ((SubTask) task).getEpicId();
            Epic epic = epics.get(epicId);

            epic.addSubTaskId(task.getTaskId());
            subTasks.put(task.getTaskId(), (SubTask) task);
        }
    }

    public static int generateTaskId() {
        return ++taskCounter;
    }

    public String getAllTasks() {
        return this.toString();
    }

    public Task getTaskById(int taskId) {
        if (tasks.containsKey(taskId)) {
            return tasks.get(taskId);

        } else if (epics.containsKey(taskId)) {
            return epics.get(taskId);

        } else if (subTasks.containsKey(taskId)) {
            return subTasks.get(taskId);
        }
        return null;
    }

    public void removeAllTasks() {
        tasks.clear();
        epics.clear();
        subTasks.clear();
        taskCounter = 0;
    }

    public void removeTaskById(int taskId) {
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
        } else if (epics.containsKey(taskId)) {
            epics.remove(taskId);
        } else if (subTasks.containsKey(taskId)) {
            int epicId = subTasks.get(taskId).getEpicId();
            Epic epic = epics.get(epicId);

            epic.removeSubTaskById(taskId);
            subTasks.remove(taskId);
        } else {
            System.out.println("Task not found");
        }
    }

    private boolean subTasksIsDone(Epic epic) {
        List<Integer> subTasksIds = epic.getSubTasksIds();
        for (int subTaskId : subTasksIds) {
            if (subTasks.get(subTaskId).getTaskStatus() != TaskStatus.DONE) {
                return false;
            }
        }
        epic.setDone(true);
        return true;
    }

    public void update(Task task) {
        if (task.getClass().equals(Task.class)) {
            updateTask(task);

        } else if (task.getClass().equals(Epic.class)) {
            updateEpic((Epic) task);

        } else if (task.getClass().equals(SubTask.class)) {
            updateSubtask((SubTask) task);
        }

    }

    private void updateTask(Task task) {
        final int taskId = task.getTaskId();

        if (tasks.containsKey(taskId)) {
            tasks.put(taskId, new Task(task));
        }
    }

    private void updateEpic(Epic epic) {
        final int taskId = epic.getTaskId();
        final TaskStatus taskStatus = epic.getTaskStatus();

        if (epics.containsKey(taskId)) {
            if (taskStatus == TaskStatus.DONE) {
                if (subTasksIsDone(epic)) {
                    epics.put(taskId, new Epic(epic, TaskStatus.DONE));
                } else {
                    System.out.println("The status \"Done\" cannot be set until the subtasks are completed.");
                }
            } else {
                epics.put(taskId, new Epic(epic));
            }
        }
    }

    private void updateSubtask(SubTask subTask) {
        final int taskId = subTask.getTaskId();
        final int epicId = subTask.getEpicId();
        final Epic epic = epics.get(epicId);

        if (subTasks.containsKey(taskId)) {
            subTasks.put(taskId, new SubTask(subTask));

            //check epic status after subtask update
            if (subTask.getTaskStatus() == TaskStatus.DONE && subTasksIsDone(epic)) {
                epic.setDone(true);
                updateEpic(new Epic(epic, TaskStatus.DONE));
            }
        }
    }


    @Override
    public String toString() {
        return "TaskManager{" +
                "tasks=" + tasks +
                ", epics=" + epics +
                ", subTasks=" + subTasks +
                '}';
    }
}