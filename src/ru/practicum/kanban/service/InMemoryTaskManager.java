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
import java.util.Objects;
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
    // For Epics, it checks for time interval overlap, then adds the epic to the epics collection and prioritizedTasks.
    // For SubTasks, it verifies that the parent epic exists, then adds the subtask to the subTasks collection;
    // its timing is managed via the parent epic, so it is not added to prioritizedTasks.
    // For regular tasks, it performs an overlap check and then adds the task.
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
                if (tasksIsOverlap(task)) {
                    throw new TaskIsOverlapException(
                            "The added task " + task.getName() + " overlaps the existing task!");
                } else {
                    tasks.put(task.getTaskId(), task);
                    addPrioritizedTasks(task);
                }
            }
        }
    }

    // Adds a task to the prioritized set if it has a specified startTime and duration.
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

    // Retrieves a task by its ID, adds it to the history, and returns the task.
    // If the task is not found, it throws an exception.
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

    // Checks whether the given task (or its corresponding subtask within an epic)
    // already exists in the prioritizedTasks set with the same ID, startTime, and duration.
    private boolean isExistInPrioritizedTasks(Task task) {
        if (task instanceof SubTask subTask) {
            Epic parentEpic = epics.get(subTask.getEpicId());
            if (parentEpic != null) {
                return parentEpic.getSubTasksIds().stream()
                        .map(subTasks::get)
                        .anyMatch(existingTask ->
                                existingTask.getTaskId() == subTask.getTaskId() &&
                                        existingTask.getStartTime().equals(subTask.getStartTime()) &&
                                        existingTask.getDuration().equals(subTask.getDuration())
                        );
            }
        }
        return prioritizedTasks.stream()
                .anyMatch(existingTask -> existingTask.getTaskId() == task.getTaskId()
                        && existingTask.getStartTime().equals(task.getStartTime())
                        && existingTask.getDuration().equals(task.getDuration())
                );
    }

    // Removes all tasks: clears the tasks, epics, subTasks, and prioritizedTasks collections, and clears the history.
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
    // Also removes the task from the history.
    @Override
    public void removeTaskById(int taskId) {
        if (tasks.containsKey(taskId)) {
            tasks.remove(taskId);
            historyManager.remove(taskId);
        } else if (epics.containsKey(taskId)) {
            // Remove the epic's subtasks before removing the epic.
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
    // If there are no subtasks, the epic retains its original values.
    private void setEpicDateTime(int epicId) {
        if (epics.containsKey(epicId)) {
            Epic epic = epics.get(epicId);
            List<Integer> subTaskIds = epic.getSubTasksIds();
            LocalDateTime startTime = epic.getStartTime();
            LocalDateTime endTime = epic.getEndTime();

            // If there are no subtasks, retain the epic's base values.
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
                    epic.getEpicStartTime(),    // retain the base startTime
                    epic.getEpicDuration()));   // retain the base duration
        }
    }

    // Returns "true" if all subtasks of the given epic have the DONE status.
    private boolean subTasksIsDone(Epic epic) {
        return epic.getSubTasksIds().stream()
                .map(subTasks::get)
                .map(SubTask::getTaskStatus)
                .allMatch(taskStatus -> taskStatus == TaskStatus.DONE);
    }

    // Checks if the given task overlaps with any of the existing tasks.
    // If the task is a subtask, its parent epic is used for the overlap check.
    // If an identical task already exists, the overlap check is skipped.
    private boolean tasksIsOverlap(Task task) {
        if (isExistInPrioritizedTasks(task)) {
            return false;
        }

        LocalDateTime taskStart = task.getStartTime();
        LocalDateTime taskEnd = task.getEndTime();

        // For subtasks, save the parent epic's ID to exclude it from the check.
        Integer epicId = null;
        if (task instanceof SubTask subTask) {
            epicId = subTask.getEpicId();
        }
        Integer parentEpicId = epicId;

        // Create a stream of existing tasks, excluding any task with the same ID
        // (to avoid checking itself during an update).
        Stream<Task> existingTasksStream = prioritizedTasks.stream()
                .filter(existingTask -> existingTask.getTaskId() != task.getTaskId())
                .flatMap(existingTask -> {
                    if (task instanceof SubTask
                            && parentEpicId == existingTask.getTaskId()
                            && existingTask instanceof Epic epic
                            && !epic.getSubTasksIds().isEmpty()) {
                        return epic.getSubTasksIds().stream()
                                .map(subTasks::get)
                                .filter(Objects::nonNull);
                    } else {
                        return Stream.of(existingTask);
                    }
                });

        return existingTasksStream
                // If updating a subtask, exclude the parent epic from the check.
                .filter(existingTask -> parentEpicId == null || (!(existingTask instanceof Epic)
                        || existingTask.getTaskId() != parentEpicId))
                .anyMatch(existingTask ->
                        taskStart.isBefore(existingTask.getEndTime()) &&
                                taskEnd.isAfter(existingTask.getStartTime())
                );
    }

    // Updates a task. Dispatches the update to a specific method based on the task type.
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

    // Updates a regular task.
    // Performs an overlap check and updates the task in both the tasks map and prioritizedTasks.
    private void updateTask(Task task) {
        final int taskId = task.getTaskId();
        if (tasks.containsKey(taskId)) {
            if (tasksIsOverlap(task)) {
                throw new TaskIsOverlapException("The updated task overlaps the existing task!");
            } else {
                updateTaskInPrioritizedTasks(tasks.get(taskId), task);
            }
        }
    }

    // Updates the epic's state, recalculates its time parameters, and adjusts its position in prioritizedTasks.
    private void updateEpic(Epic epic) {
        final int taskId = epic.getTaskId();
        if (epics.containsKey(taskId)) {
            if (tasksIsOverlap(epic)) {
                throw new TaskIsOverlapException("The updated task overlaps the existing task!");
            } else {
                prioritizedTasks.remove(epics.get(epic.getTaskId()));

                if (epic.getTaskStatus() == TaskStatus.DONE && subTasksIsDone(epic)) {
                    epics.put(epic.getTaskId(), new Epic(epic, TaskStatus.DONE));
                } else if (epic.getTaskStatus() != TaskStatus.DONE) {
                    epics.put(epic.getTaskId(), new Epic(epic));
                }
                setEpicDateTime(epic.getTaskId());
                prioritizedTasks.add(epic);
            }
        }
    }

    // Updates a subtask. Performs an overlap check, updates the subtask in the subTasks collection,
    //and then updates the parent epic's status and time parameters if necessary.
    private void updateSubtask(SubTask subTask) {
        final int taskId = subTask.getTaskId();
        final int epicId = subTask.getEpicId();

        if (subTasks.containsKey(taskId) && epics.containsKey(epicId)) {
            if (tasksIsOverlap(subTask)) {
                throw new TaskIsOverlapException("The updated task overlaps the existing task!");
            } else {
                final Epic epic = epics.get(epicId);
                subTasks.put(taskId, new SubTask(subTask));

                // Check the epic's status after updating the subtask.
                if (subTask.getTaskStatus() == TaskStatus.DONE && subTasksIsDone(epic)) {
                    updateEpic(new Epic(epic, TaskStatus.DONE));
                    setEpicDateTime(epicId);
                }
            }
        } else {
            throw new NoSuchElementException("Unable to update subtask: an epic with this ID does not exist.");
        }
    }

    // Updates a task in the tasks map and in prioritizedTasks.
    private void updateTaskInPrioritizedTasks(Task oldTask, Task newTask) {
        prioritizedTasks.remove(oldTask);
        tasks.put(newTask.getTaskId(), new Task(newTask));
        prioritizedTasks.add(newTask);
    }
}