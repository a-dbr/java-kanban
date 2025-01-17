/*
* Пытался в полиморфизм, вышло так себе
*/

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

    public TaskManager() {
        tasks = new HashMap<>();
    }

    public String getAllTasks() {
        return this.toString();
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Task getTask(Object o) {
        if (o.getClass().equals(Task.class) || o.getClass().equals(Epic.class)) {
            Task task = (Task) o;
            return tasks.get(task.getTaskID());
        } else if (o.getClass().equals(SubTask.class)) {
            SubTask task = (SubTask) o;
            int epicID = task.getEpicID();
            Epic epic = (Epic) tasks.get(epicID);
            return epic.getSubTask(task);
        }
        return null; // try/catch еще не изучали
    }

    public void addTask(Object o) {
        if (o.getClass().equals(Task.class)) {
            Task task = (Task) o;
            tasks.put(task.getTaskID(), task);

        } else if (o.getClass().equals(Epic.class)) {
            Epic epic = (Epic) o;
            tasks.put(epic.getTaskID(), epic);
        } else if (o.getClass().equals(SubTask.class)) {
            SubTask subTask = (SubTask) o;
            int epicID = subTask.getEpicID();

            Epic epic = (Epic) tasks.get(epicID);
            epic.addSubTask(subTask);
        }
    }

    public void updateTask(Object o) {  //automatic task status update
        if (o.getClass().equals(Task.class)) {
            Task task = (Task) o;
            final int taskID = task.getTaskID();

            if (tasks.containsKey(taskID) && (task.getTaskStatus() != TaskStatus.DONE)) {
                task = tasks.get(taskID);
                tasks.put(taskID, new Task(task));
            }

        } else if (o.getClass().equals(Epic.class)) {
            Epic epic = (Epic) o;
            final int epicID = epic.getTaskID();

            if (tasks.containsKey(epicID) && (epic.getTaskStatus() != TaskStatus.DONE)) {
                epic = (Epic) tasks.get(epicID);
                if (epic.getTaskStatus() == TaskStatus.NEW) {
                    tasks.put(epicID, new Epic(epic));

                    if (!epic.getSubTasks().isEmpty()) {
                        List<SubTask> subTasks = epic.getSubTasks();
                        for (int i = 0; i < subTasks.size(); i++) {
                            subTasks.set(i, new SubTask(subTasks.get(i), epicID));
                        }
                    }

                } else if (epic.getTaskStatus() == TaskStatus.IN_PROGRESS && epic.isDone()) {
                    tasks.put(epic.getTaskID(), new Epic(epic));
                }
            }

        } else if (o.getClass().equals(SubTask.class)) {
            SubTask subTask = (SubTask) o;
            int epicID = subTask.getEpicID();
            final Epic epic = (Epic) tasks.get(epicID);
            List<SubTask> subTasksList = epic.getSubTasks();

            if (subTasksList.contains(subTask)) {
                int index = subTasksList.indexOf(subTask);
                subTasksList.set(index, new SubTask(subTasksList.get(index), epicID));

                if (subTasksIsDone(epic)) {  //check epic status after subtask update
                    updateTask(epic);
                }
            }
        }

    }

    private boolean subTasksIsDone(Epic epic) {
        List<SubTask> subTasks = epic.getSubTasks();
        for (SubTask subTask : subTasks) {
            if (subTask.getTaskStatus() != TaskStatus.DONE) {
                return false;
            }
        }
        epic.setDone(true);
        return true;
    }

    public void removeTask(Object o) {
        if (o.getClass().equals(Task.class) || o.getClass().equals(Epic.class)) {
            Task task = (Task) o;
            tasks.remove(task.getTaskID());
        } else if (o.getClass().equals(SubTask.class)) {
            SubTask task = (SubTask) o;
            int epicID = task.getEpicID();
            Epic epic = (Epic) tasks.get(epicID);
            epic.removeSubTask(task);
        }
    }

    @Override
    public String toString() {
        return "TaskManager{" +
                "tasks=" + tasks +
                '}';
    }
}
