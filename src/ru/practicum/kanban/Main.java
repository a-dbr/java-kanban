package ru.practicum.kanban;

import ru.practicum.kanban.service.Managers;
import ru.practicum.kanban.service.TaskManager;
import ru.practicum.kanban.model.enums.TaskStatus;
import ru.practicum.kanban.model.Epic;
import ru.practicum.kanban.model.SubTask;
import ru.practicum.kanban.model.Task;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    private final static String FILE_NAME = "./src/ru/practicum/kanban/resources/db.csv";

    public static void main(String[] args) {

        Path file = Paths.get(FILE_NAME);

        TaskManager taskManager = Managers.getDefault(file);

        System.out.println("Adding a task and changing the status:");
        Task task1 = new Task("Task name", "Task description");
        taskManager.addTask(task1);
        System.out.println(taskManager.getTaskById(task1.getTaskId()));

        //Emulating task update
        Task newTask1 = new Task(
                task1.getName(), //We take the same name. We can change it.
                task1.getDescription(), //Or description
                TaskStatus.IN_PROGRESS,  //Setting a new task status
                task1.getTaskId() //We MUST take the taskID from the original task.
        );
        taskManager.update(newTask1);
        System.out.println(taskManager.getTaskById(task1.getTaskId())); //print
        System.out.println();
        //Done

        // test
        System.out.println("Adding an epic, subtasks, and changing the status:");
        Task task2 = new Epic("Task2 name", "Task2 description");
        Task subTask1 = new SubTask("Subtask1", "Subtask1 description", task2.getTaskId());
        Task subTask2 = new SubTask("Subtask2", "Subtask2 description", task2.getTaskId());

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
                ((Epic) task2).getSubTasksIds()
        ));

        task2 = taskManager.getTaskById(task2.getTaskId());
        System.out.println(task2);

        taskManager.update(new SubTask(
                subTask1.getName(),
                subTask1.getDescription(),
                TaskStatus.DONE,
                subTask1.getTaskId(),
                ((SubTask) subTask1).getEpicId())
        );

        taskManager.update(new SubTask(
                subTask2.getName(),
                subTask2.getDescription(),
                TaskStatus.DONE,
                subTask2.getTaskId(),
                ((SubTask) subTask2).getEpicId())
        );

        task2 = taskManager.getTaskById(task2.getTaskId());

        System.out.println(task2);
        System.out.println();

        System.out.println("Deleting a task and deleting a task from the browsing history:");
        Task task3 = new Task("Task3", "Description");
        taskManager.addTask(task3);
        task3 = taskManager.getTaskById(task3.getTaskId());
        System.out.println(task3);
        System.out.println("Browsing history:");
        System.out.println(taskManager.getHistory());
        System.out.println();

        taskManager.removeTaskById(task3.getTaskId());
        //taskManager.removeTaskById(subTask2.getTaskId());

        System.out.println("Browsing history after deleting a task:");
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
