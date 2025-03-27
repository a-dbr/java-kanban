package ru.practicum.kanban.exceptions;

public class TaskIsOverlapException extends RuntimeException {
    public TaskIsOverlapException(String message) {
        super(message);
    }
}
