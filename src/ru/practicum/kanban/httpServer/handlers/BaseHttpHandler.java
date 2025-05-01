package ru.practicum.kanban.httpServer.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler  implements HttpHandler {

    protected void sendText(HttpExchange h, String text, int code) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange httpExchange, String message) throws IOException {
        sendText(httpExchange, message, 404);
    }

    protected void sendHasIntersection(HttpExchange httpExchange, String message) throws IOException {
        sendText(httpExchange, message, 406);
    }

    protected void sendBadRequest(HttpExchange httpExchange) throws IOException {
        sendText(httpExchange, "Bad request", 400);
    }

    protected void sendMethodNotFound(HttpExchange httpExchange, String message) throws IOException {
        sendText(httpExchange, message, 405);
    }

    protected void sendInternalError(HttpExchange httpExchange, String message) throws IOException {
        sendText(httpExchange, message, 500);
    }

    protected int getTaskId(String path) throws IOException {
        String[] arrayPath = path.split("/");
        return Integer.parseInt(arrayPath[2]);
    }
}
