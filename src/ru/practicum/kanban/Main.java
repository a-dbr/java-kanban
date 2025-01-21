package ru.practicum.kanban;

import ru.practicum.kanban.service.TaskManager;
import ru.practicum.kanban.status.TaskStatus;
import ru.practicum.kanban.tasks.Epic;
import ru.practicum.kanban.tasks.SubTask;
import ru.practicum.kanban.tasks.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

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
        //Done

        // test
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
                ((Epic) task2).getSubTasksIds(),
                ((Epic) task2).isDone()
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

        Task task3 = new Task("Жывтоне чочо упячка", "Шячло попячтса");
        taskManager.addTask(task3);

        taskManager.removeTaskById(task3.getTaskId());
        taskManager.removeTaskById(subTask2.getTaskId());

        System.out.println(taskManager.getAllTasks());
        System.out.println();

        taskManager.removeAllTasks();
        System.out.println(taskManager.getAllTasks());
    }
}
