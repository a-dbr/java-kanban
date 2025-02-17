package ru.practicum.kanban.service;

import ru.practicum.kanban.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList history = new CustomLinkedList();

    @Override
    public void addTask(Task task) {
        history.linkLast(task);
    }

    @Override
    public List<Task> getHistory() {
        return history.asList();
    }

    @Override
    public void remove(int id) {
        history.removeNode(id);
    }
}

class CustomLinkedList {
    private Node<Task> head;
    private Node<Task> tail;
    int size;

    Map<Integer, Node<Task>> nodesId = new HashMap<>();

    public List<Task> asList() {
        List<Task> arrayList = new ArrayList<>();
        Node<Task> current = head;
        while (current != null) {
            arrayList.add(current.getData());
            current = current.getNext();
        }
        return arrayList;
    }

    public void linkLast(Task task) {
        if (nodesId.containsKey(task.getTaskId())) {
            removeNode(nodesId.get(task.getTaskId()));
        }

        final Node<Task> oldTail = tail;
        final Node<Task> newNode = new Node<>(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.setNext(newNode);
        }
        size++;

        nodesId.put(task.getTaskId(), newNode);
    }

    public void removeNode(int id) {
        removeNode(nodesId.get(id));
    }

    public void removeNode(Node<Task> node) {
        Node<Task> prev = node.getPrev();
        Node<Task> next = node.getNext();

        if (prev == null) {
            head = next;
        } else {
            prev.setNext(next);
        }

        if (next == null) {
            tail = prev;
        } else {
            next.setPrev(prev);
        }
    }
}
