package ru.practicum.kanban.service;

import ru.practicum.kanban.exceptions.TaskIsOverlapException;
import ru.practicum.kanban.model.enums.TaskStatus;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.SubTask;
import ru.practicum.kanban.model.Task;

import java.util.Collection;
import java.util.Comparator;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.List;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, SubTask> subTasks;
    private final Set<Task> prioritizedTasks;
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    public static int taskCounter;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        taskCounter = 0;
    }

    // Adds a task to the corresponding collection.
    // The method accepts objects cast to the Task type.
    // Subtasks are not added to the prioritizedTasks set since they are part of epics.
    @Override
    public void addTask(Task task) {
        switch (task) {
            case null -> throw new NoSuchElementException("The task cannot be null");
            case Epic epic -> {
                // Check for time interval overlap; if an overlap is found, throw an exception.
                if (tasksIsOverlap(task)) {
                    throw new TaskIsOverlapException(
                            "The added task " + task.getName() + " overlaps the existing task!");
                } else {
                    epics.put(task.getTaskId(), epic);
                    addPrioritizedTasks(task);
                }
            }
            case SubTask subTask -> {
                int epicId = subTask.getEpicId();
                if (epics.containsKey(epicId)) {
                    if (tasksIsOverlap(task)) {
                        throw new TaskIsOverlapException(
                                "The added task " + task.getName() + " overlaps the existing task!");
                    } else {
                        Epic epic = epics.get(epicId);
                        epic.addSubTaskId(task.getTaskId());
                        subTasks.put(task.getTaskId(), (SubTask) task);
                        setEpicDateTime(epicId);
                    }
                } else {
                    throw new NoSuchElementException("Unable to add subtask: an epic with this ID does not exist.");
                }
            }
            default -> {
                tasks.put(task.getTaskId(), task);
                addPrioritizedTasks(task);
            }
        }
    }

    // Adds a task to the prioritized set if start time and duration are specified.
    // This is needed to ensure correct task sorting by start time.
    private void addPrioritizedTasks(Task task) {
        if (task.getStartTime() != null && task.getDuration() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public List<Task> getAllTasks() {
        return Stream.of(tasks.values(), epics.values(), subTasks.values())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    // Retrieves a task by its ID, adds it to the history, and returns it.
    // If the task is not found, an exception is thrown.
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
        throw new NoSuchElementException("Task with ID " + taskId + " not found.");
    }

    // Checks if a task with the same ID, startTime, and duration exists in the prioritized set.
    // This is used to avoid rechecking time overlap when updating a task.
    private boolean isExistInPrioritizedTasks(Task task) {
        return prioritizedTasks.stream()
                .anyMatch(existingTask -> existingTask.getTaskId() == task.getTaskId()
                        && existingTask.getStartTime().equals(task.getStartTime())
                        && existingTask.getDuration().equals(task.getDuration())
                );
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
        epics.clear();
        subTasks.clear();
        historyManager.removeAll();
        prioritizedTasks.clear();
        taskCounter = 0;
    }

    // Removes a task by its ID.
    // For epics, all subtasks are removed first.
    @Override
    public void removeTaskById(int taskId) {
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
            historyManager.remove(taskId);
        } else if (epics.containsKey(taskId)) {
            // Remove subtasks of the epic before removing the epic.
            epics.get(taskId).getSubTasksIds().forEach(this::removeTaskById);

            epics.remove(taskId);
            historyManager.remove(taskId);
        } else if (subTasks.containsKey(taskId)) {
            int epicId = subTasks.get(taskId).getEpicId();
            Epic epic = epics.get(epicId);

            epic.removeSubTaskById(taskId);
            subTasks.remove(taskId);
            historyManager.remove(taskId);
            setEpicDateTime(epicId);
        } else {
            System.out.println("Task not found");
        }
    }

    // Updates the epic's start time and duration based on its subtasks.
    // If no subtasks exist, the epic's original values remain unchanged.
    private void setEpicDateTime(int epicId) {
        if (epics.containsKey(epicId)) {
            Epic epic = epics.get(epicId);
            List<Integer> subTaskIds = epic.getSubTasksIds();
            LocalDateTime startTime = epic.getStartTime();
            LocalDateTime endTime = epic.getEndTime();

            // If there are no subtasks, keep the epic's base values
            if (subTaskIds.isEmpty()) {
                epics.put(epicId, new Epic(
                        epic,
                        epic.getStartTime(),
                        epic.getDuration(),
                        epic.getEpicStartTime(),
                        epic.getEpicDuration()));
                return;
            }

            // Find the earliest start time among the subtasks
            LocalDateTime minStartTime = subTaskIds.stream()
                    .map(subTasks::get)
                    .map(SubTask::getStartTime)
                    .min(LocalDateTime::compareTo)
                    .orElse(startTime);

            // Find the latest end time among the subtasks
            LocalDateTime maxEndTime = subTaskIds.stream()
                    .map(subTasks::get)
                    .map(SubTask::getEndTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(epic.getEndTime());

            // Determine the new start time as the earlier one between the epic's base and the earliest subtask start time
            LocalDateTime newStartTime = (minStartTime.isBefore(startTime)) ? minStartTime : startTime;

            // Determine the new end time as the later one between the epic's base and the latest subtask end time
            LocalDateTime newEndTime = (maxEndTime.isAfter(endTime)) ? maxEndTime : endTime;

            // Calculate the new duration as the difference between the new start time and new end time
            Duration newDuration = Duration.between(newStartTime, newEndTime);

            // Create a new Epic instance with updated time parameters
            epics.put(epicId, new Epic(
                    epic,
                    newStartTime,
                    newDuration,
                    epic.getEpicStartTime(),    // keep the base start time
                    epic.getEpicDuration()));   // keep the base duration
        }
    }

    private boolean subTasksIsDone(Epic epic) {
        return epic.getSubTasksIds().stream()
                .map(subTasks::get)
                .map(SubTask::getTaskStatus)
                .allMatch(taskStatus -> taskStatus == TaskStatus.DONE);
    }

    // Checks if the time interval of the given task overlaps with existing tasks.
    // If the task is a subtask, its parent epic is used for overlap checking.
    private boolean tasksIsOverlap(Task task) {
        // If the task is a subtask, replace it with its parent epic for checking.
        if (task instanceof SubTask && epics.containsKey(task.getTaskId())) {
            task = epics.get(((SubTask) task).getEpicId());
        }

        Task finalTask = task;

        // If a task with the same time parameters already exists, no further overlap check is needed.
        if (isExistInPrioritizedTasks(task)) {
            return false;
        }

        // Check for time interval overlap in the prioritized tasks.
        // For an epic with subtasks, check the time intervals of the subtasks;
        // otherwise, check the task's own time interval.
        return prioritizedTasks.stream()
                .flatMap(existingTask -> {
                    if (existingTask instanceof Epic && !((Epic) existingTask).getSubTasksIds().isEmpty()) {
                        return ((Epic) existingTask).getSubTasksIds().stream()
                                .map(subTasks::get)
                                .filter(this::isExistInPrioritizedTasks); // Additionally filter subtasks present in prioritizedTasks
                    } else {
                        return Stream.of(existingTask);
                    }
                })
                .anyMatch(existingTask ->
                        finalTask.getStartTime().isBefore(existingTask.getEndTime())
                                && finalTask.getEndTime().isAfter(existingTask.getStartTime()));
    }

    @Override
    public void update(Task task) {
        if (task instanceof Epic) {
            updateEpic((Epic) task);
        } else if (task instanceof SubTask) {
            updateSubtask((SubTask) task);
        } else {
            updateTask(task);
        }
    }

    private void updateTask(Task task) {
        final int taskId = task.getTaskId();

        if (tasks.containsKey(taskId)) {
            if (tasksIsOverlap(task)) {
                throw new TaskIsOverlapException("The updated task overlaps the existing task!");
            } else {
                tasks.put(taskId, new Task(task));
            }
        }
    }

    private void updateEpic(Epic epic) {
        final int taskId = epic.getTaskId();
        if (epics.containsKey(taskId)) {
            if (tasksIsOverlap(epic)) {
                throw new TaskIsOverlapException("The updated task overlaps the existing task!");
            } else {
                if (epic.getTaskStatus() == TaskStatus.DONE && subTasksIsDone(epic)) {
                    epics.put(epic.getTaskId(), new Epic(epic, TaskStatus.DONE));
                    setEpicDateTime(epic.getTaskId());
                } else if (epic.getTaskStatus() != TaskStatus.DONE) {
                    epics.put(epic.getTaskId(), new Epic(epic));
                    setEpicDateTime(epic.getTaskId());
                }
            }
        }
    }

    private void updateSubtask(SubTask subTask) {
        final int taskId = subTask.getTaskId();
        final int epicId = subTask.getEpicId();

        if (subTasks.containsKey(taskId) && epics.containsKey(epicId)) {
            if (tasksIsOverlap(subTask)) {
                throw new TaskIsOverlapException("The updated task overlaps the existing task!");
            } else {
                final Epic epic = epics.get(epicId);
                subTasks.put(taskId, new SubTask(subTask));

                // Checking the epic's status after updating a subtask.
                if (subTask.getTaskStatus() == TaskStatus.DONE && subTasksIsDone(epic)) {
                    updateEpic(new Epic(epic, TaskStatus.DONE));
                    setEpicDateTime(epicId);
                }
            }
        } else {
            throw new NoSuchElementException("Unable to update subtask: an epic with this ID does not exist.");
        }
    }
}