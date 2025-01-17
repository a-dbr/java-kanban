package ru.practicum.kanban;

import ru.practicum.kanban.service.TaskManager;
import ru.practicum.kanban.tasks.Epic;
import ru.practicum.kanban.tasks.SubTask;
import ru.practicum.kanban.tasks.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("Работа в праздничные", "Проработать все праздники");
        taskManager.addTask(task1);
        System.out.println(taskManager.getTask(task1));

        taskManager.updateTask(task1);
        task1 = taskManager.getTask(task1);
        System.out.println(taskManager.getTask(task1));

        taskManager.updateTask(task1);

        System.out.println(taskManager.getTask(task1));
        System.out.println();

        Task task2 = new Epic("Работы в ЦОДе", "");
        SubTask subTask1 = new SubTask("Заменить циски на элтекс", "ну и настроить", task2.getTaskID());
        SubTask subTask2 = new SubTask("Оптимизация работы терминальной фермы",
                "Выяснить причину неравномерной нагрузки на серверы", task2.getTaskID());

        taskManager.addTask(task2);
        taskManager.addTask(subTask1);
        taskManager.addTask(subTask2);
        System.out.println(task2);

        taskManager.updateTask(task2);
        task2 = taskManager.getTask(task2);

        System.out.println(task2);

        taskManager.updateTask(subTask1);
        taskManager.updateTask(subTask2);
        task2 = taskManager.getTask(task2);

        System.out.println(task2);
        System.out.println();

        Task task3 = new Task("Жывтоне чочо упячка", "Шячло попячтса");
        taskManager.addTask(task3);

        taskManager.removeTask(task3);
        taskManager.removeTask(subTask2);

        System.out.println(taskManager.getAllTasks());
        System.out.println();

        taskManager.deleteAllTasks();
        System.out.println(taskManager.getAllTasks());


    }
}
