package ru.practicum.kanban.service;

import ru.practicum.kanban.status.TaskStatus;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.SubTask;
import ru.practicum.kanban.model.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, SubTask> subTasks;
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    public static int taskCounter;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();

        taskCounter = 0;
    }

    @Override
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

    @Override
    public List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>();

        if (!tasks.isEmpty()) {
            allTasks.addAll(tasks.values());
        }
        if (!epics.isEmpty()) {
            allTasks.addAll(epics.values());
        }
        if (!subTasks.isEmpty()) {
            allTasks.addAll(subTasks.values());
        }

        return allTasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Task getTaskById(int taskId) {
        if (tasks.containsKey(taskId)) {
            historyManager.addTask(tasks.get(taskId));
            return tasks.get(taskId);

        } else if (epics.containsKey(taskId)) {
            historyManager.addTask(epics.get(taskId));
            return epics.get(taskId);

        } else if (subTasks.containsKey(taskId)) {
            historyManager.addTask(subTasks.get(taskId));
            return subTasks.get(taskId);
        }
        // If the task is not found, throw an exception.
        throw new NoSuchElementException("Task with ID " + taskId + " not found.");
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
        epics.clear();
        subTasks.clear();
        taskCounter = 0;
    }

    @Override
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

    @Override
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
}