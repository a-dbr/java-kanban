package ru.practicum.kanban.service;

import ru.practicum.kanban.model.Task;

import java.util.*;

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
        history.removeNodeById(id);
    }

    @Override
    public void removeAll() {
        history.removeAllNodes();
    }

    class CustomLinkedList {
        private Node<Task> head;
        private Node<Task> tail;

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

            nodesId.put(task.getTaskId(), newNode);
        }

        public void removeAllNodes() {
            for (Node<Task> node : nodesId.values()) {
                removeNode(node);
            }
            nodesId.clear();
        }

        public void removeNodeById(int id) {
            removeNode(nodesId.get(id));
        }

        public void removeNode(Node<Task> node) {
            if (node != null) {
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
    }
}


