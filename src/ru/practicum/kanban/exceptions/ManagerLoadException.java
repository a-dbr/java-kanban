package ru.practicum.kanban.exceptions;

public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(final String message) {
        super(message);
    }
}
