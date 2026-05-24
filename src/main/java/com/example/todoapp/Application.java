package com.example.todoapp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

/**
 * Main class of the application. Managing routing and HTTP layer.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private static final TaskDao dao = new TaskDao();

    public static void main(String[] args) throws Exception {
        log.info("In-memory repository initialised");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/tasks", Application::handleTasks);
        server.setExecutor(null);
        server.start();
        log.info("HTTP server started on http://localhost:8080");
    }

    private static void handleTasks(HttpExchange exchange) throws IOException {
        // Allow cross-origin call (call from the frontend)
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        //Manage /tasks
        if ("/tasks".equals(path)) {
            //region Manage POST /tasks
            if("POST".equals(method)) {
                Task input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), Task.class);
                int newId = dao.calculateNextId();
                Task newTask = new Task(newId, input.title(), input.description(), input.done());
                Task createdTask = dao.save(newTask);

                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
                sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
                return;
            }
            //endregion

            //region Manage GET /tasks
            if ("GET".equals(method)) {
                boolean todoOnly = query.equals("todo-only=true");
                Collection<Task> taskList = dao.getTasksList(todoOnly);

                if (taskList.isEmpty()) {
                    sendResponse(exchange, 204, null);
                }
                else {
                    sendResponse(exchange, 200, JsonUtils.serialize(taskList));
                }
                return;
            }
            //endregion
        }


        // Manage /tasks/{id}
        Matcher m = ID_PATH.matcher(path);
        if (m.matches()) {
            int id = Integer.parseInt(m.group(1));
            Optional<Task> task = dao.findById(id);

            if (task.isEmpty()) {
                sendResponse(exchange, 404, null);
                return;
            }

            //region Manage GET /tasks/{id}
            if ("GET".equals(method)) {
                sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
                return;
            }
            //endregion

            //region Manage DELETE /tasks/{id}
            if ("DELETE".equals(method)){
                dao.deleteTaskById(id);
                sendResponse(exchange, 204, null);
                return;
            }
            //endregion

            //region Manage PUT /tasks/{id}
            if ("PUT".equals(method)) {
                InputStream bodyStream = exchange.getRequestBody();
                String body = new String(bodyStream.readAllBytes());

                Task updatedTask = JsonUtils.deserialize(body, Task.class);
                dao.updateTaskById(id, updatedTask.title(), updatedTask.description(), updatedTask.done());
                sendResponse(exchange, 204, null);
                return;
            }
            //endregion
        }

        // Otherwise → 404
        sendResponse(exchange, 404, null);
    }

    private static void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Allow data fetch from the local frontend

        if(nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, 0);
            exchange.close();
        }
    }
}
