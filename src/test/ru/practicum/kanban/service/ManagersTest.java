package ru.practicum.kanban.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ManagersTest {

    @Test
    void shouldNotReturnNull() {
        Assertions.assertNotNull(Managers.getDefault(),
                "getDefault() should not return a null value.");

        Assertions.assertNotNull(Managers.getDefaultHistory(),
                "getDefaultHistory() should not return a null value.");
    }

    @Test
    void shouldReturnDifferentInstances() {
        TaskManager taskManager1 = Managers.getDefault();
        TaskManager taskManager2 = Managers.getDefault();

        Assertions.assertNotSame(taskManager1, taskManager2,
                "getDefault() should return different objects");
    }


}