package ru.practicum.kanban.service;

public class TaskCsvFormatHandler {
    protected static String getHeader() {
        return "id,type,name,status,description,epic";
    }
}
