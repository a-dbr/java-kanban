package ru.practicum.kanban;

import ru.practicum.kanban.service.Managers;
import ru.practicum.kanban.service.TaskManager;
import ru.practicum.kanban.model.enums.TaskStatus;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.SubTask;
import ru.practicum.kanban.model.Task;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        final String fileName = "./src/ru/practicum/kanban/resources/db.csv";
        Path file = Paths.get(fileName);

        TaskManager taskManager = Managers.getDefault(file);

        //TaskManager taskManager = Managers.getDefault();

        System.out.println("Adding a task and changing the status:");
        Task task1 = new Task(
                "Task name",
                "Task description",
                LocalDateTime.of(2025, 1, 1, 0,0),
                Duration.ofHours(1));
        taskManager.addTask(task1);
        System.out.println(taskManager.getTaskById(task1.getTaskId()));

        //Emulating task update
        Task newTask1 = new Task(
                task1.getName(), //We take the same name. We can change it.
                task1.getDescription(), //Or description
                TaskStatus.IN_PROGRESS,  //Setting a new task status
                task1.getTaskId(), //We MUST take the taskID from the original task.
                task1.getStartTime(),
                task1.getDuration());

        taskManager.update(newTask1);
        System.out.println(taskManager.getTaskById(task1.getTaskId())); //print
        System.out.println();
        //Done

        // test
        System.out.println("Adding an epic, subtasks, and changing the status:");
        Task task2 = new Epic(
                "Task2 name",
                "Task2 description",
                LocalDateTime.of(2025, 1, 5, 0,0),
                Duration.ofHours(1));

        Task subTask1 = new SubTask(
                "Subtask1",
                "Subtask1 description",
                task2.getTaskId(),
                LocalDateTime.of(2025, 2, 5, 0,0),
                Duration.ofHours(10));
        Task subTask2 = new SubTask(
                "Subtask2",
                "Subtask2 description",
                task2.getTaskId(),
                LocalDateTime.of(2025, 3, 12, 0,0),
                Duration.ofHours(8));

        taskManager.addTask(task2);
        taskManager.addTask(subTask1);
        taskManager.addTask(subTask2);

        task2 = taskManager.getTaskById(task2.getTaskId());
        System.out.println(task2);

        taskManager.update(new Epic(
                task2.getName(),
                task2.getDescription(),
                TaskStatus.IN_PROGRESS,
                task2.getTaskId(),
                ((Epic) task2).getSubTasksIds(),
                LocalDateTime.of(2025, 6, 5, 0,0),
                task2.getDuration(),
                LocalDateTime.of(2025, 6, 5, 0,0),
                ((Epic) task2).getEpicDuration()));

        task2 = taskManager.getTaskById(task2.getTaskId());

        System.out.println("Epic start time:");
        System.out.println(((Epic) task2).getEpicStartTime());

        System.out.println("Epic start time with subtasks:");
        System.out.println(task2.getStartTime());
        System.out.println("End time:");
        System.out.println(task2.getEndTime());

        taskManager.update(new SubTask(
                subTask1.getName(),
                subTask1.getDescription(),
                TaskStatus.DONE,
                subTask1.getTaskId(),
                ((SubTask) subTask1).getEpicId(),
                subTask1.getStartTime(),
                subTask1.getDuration()));

        taskManager.update(new SubTask(
                subTask2.getName(),
                subTask2.getDescription(),
                TaskStatus.DONE,
                subTask2.getTaskId(),
                ((SubTask) subTask2).getEpicId(),
                subTask2.getStartTime(),
                subTask2.getDuration()));

        System.out.println("Prioritized tasks:");
        System.out.println(taskManager.getPrioritizedTasks());


        System.out.println("Browsing history:");
        System.out.println(taskManager.getHistory());
        System.out.println();

        System.out.println("AllTasks");
        System.out.println(taskManager.getAllTasks());
        System.out.println();

        taskManager.removeAllTasks();
        System.out.println("AllTasks(empty)");
        System.out.println(taskManager.getAllTasks());

        System.out.println("Browsing history:");
        System.out.println(taskManager.getHistory());
        System.out.println();
    }
}
