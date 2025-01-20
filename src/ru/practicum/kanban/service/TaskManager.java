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
            subTasks.put(task.getTaskId(), (SubTask) task);
            epics.get(((SubTask) task).getEpicId()).addSubTaskId(task.getTaskId());
        }
    }

    public static int generateTaskId() {
        return ++taskCounter;
    }

    public String getAllTasks() {
        return this.toString();
    }

    public Task getTask(Task task) {
        if (task.getClass().equals(Task.class)) {
            return tasks.get(task.getTaskId());
        } else if (task.getClass().equals(Epic.class)) {
            return epics.get(task.getTaskId());
        } else if (task.getClass().equals(SubTask.class)) {
            return subTasks.get(task.getTaskId());
        }
        return null;
    }

    public void removeAllTasks() {
        tasks.clear();
        epics.clear();
        subTasks.clear();
        taskCounter = 0;
    }

    public void removeTask(Task task) {
        if (task.getClass().equals(Task.class)) {
            tasks.remove(task.getTaskId());
        } else if (task.getClass().equals(Epic.class)) {
            epics.remove(task.getTaskId());
        } else if (task.getClass().equals(SubTask.class)) {
            Epic epic = epics.get(((SubTask) task).getEpicId());

            epic.removeSubTaskById(task.getTaskId());
            subTasks.remove(task.getTaskId());
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

    public void updateTask(Task task) {  //automatic task status update
        final int taskId = task.getTaskId();

        if (task.getClass().equals(Task.class)) {
            if (tasks.containsKey(taskId) && (task.getTaskStatus() != TaskStatus.DONE)) {
                tasks.put(taskId, new Task(tasks.get(taskId)));
            }
        } else if (task.getClass().equals(Epic.class)) {
            if (epics.containsKey(taskId) && ((task.getTaskStatus() != TaskStatus.DONE))) {
                if (task.getTaskStatus() == TaskStatus.NEW) {
                    epics.put(taskId, new Epic((Epic) task)); //setting epic status IN_PROGRESS

                    if (!((Epic) task).getSubTasksIds().isEmpty()) { //updating subtask statuses
                        for (int subTaskId : ((Epic) task).getSubTasksIds()) {
                            subTasks.put(subTaskId, new SubTask(subTasks.get(subTaskId), taskId));
                        }
                    }
                } else if (task.getTaskStatus() == TaskStatus.IN_PROGRESS && ((Epic) task).isDone()) {
                    epics.put(task.getTaskId(), new Epic((Epic) task));
                }
            }
        } else if (task.getClass().equals(SubTask.class)) {
            final Epic epic = epics.get(((SubTask) task).getEpicId());

            if (subTasks.containsKey(taskId)) {
                subTasks.put(taskId, new SubTask((SubTask) task, ((SubTask) task).getEpicId()));

                if (subTasksIsDone(epic)) {  //check epic status after subtask update
                    updateTask(epic);
                }
            }
        }

    }

    @Override
    public String toString() {
        return "TaskManager{" +
                "tasks=" + tasks +
                ", epics=" + epics +
                ", subTasks=" + subTasks +
                ", taskCounter=" + taskCounter +
                '}';
    }
}