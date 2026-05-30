package com.example.todoapp.presentation;

import com.example.todoapp.JsonUtils;
import com.example.todoapp.business.service.TaskService;
import com.example.todoapp.dto.TaskErrorDTO;
import com.example.todoapp.dto.TaskOutputDTO;
import com.example.todoapp.dto.TaskPostDTO;
import com.example.todoapp.dto.TaskPutDTO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

public class TaskController implements HttpHandler {
    private final TaskService taskService = new TaskService();
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");


    public void handle(HttpExchange exchange) throws IOException {
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
                TaskPostDTO input = JsonUtils.deserialize(new String(exchange.getRequestBody().readAllBytes(), UTF_8), TaskPostDTO.class);

                if (input.title() == null || input.title().isEmpty()) {
                    TaskErrorDTO error = new TaskErrorDTO("title", "Title should not be empty.");
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                }

                if (input.title() != null && input.title().length() > 50) {
                    TaskErrorDTO error = new TaskErrorDTO("title", "Title should not exceed 50 characters.");
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                }

                if ( input.description() !=null && input.description().length() > 255 ) {
                    TaskErrorDTO error = new TaskErrorDTO("description", "Description should not exceed 255 characters.");
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                }

                TaskOutputDTO createdTask = this.taskService.createTask(input);

                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
                sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
                return;
            }
            //endregion

            //region Manage GET /tasks
            if ("GET".equals(method)) {
                boolean todoOnly = query.equals("todo-only=true");
                Collection<TaskOutputDTO> output = this.taskService.getTasksList(todoOnly);

                if (output.isEmpty()) {
                    sendResponse(exchange, 204, null);
                }
                else {
                    sendResponse(exchange, 200, JsonUtils.serialize(output));
                }
                return;
            }
            //endregion
        }


        // Manage /tasks/{id}
        Matcher m = ID_PATH.matcher(path);

        if (m.matches()) {
            int id = Integer.parseInt(m.group(1));
            Optional<TaskOutputDTO> output = this.taskService.getTaskById(id);

            if (output.isEmpty()) {
                sendResponse(exchange, 404, null);
                return;
            }

            //region Manage GET /tasks/{id}
            if ("GET".equals(method)) {
                sendResponse(exchange, 200, JsonUtils.serialize(output));
                return;
            }
            //endregion

            //region Manage DELETE /tasks/{id}
            if ("DELETE".equals(method)){
                this.taskService.deleteTaskById(id);
                sendResponse(exchange, 204, null);
                return;
            }
            //endregion

            //region Manage PUT /tasks/{id}
            if ("PUT".equals(method)) {
                InputStream bodyStream = exchange.getRequestBody();
                String body = new String(bodyStream.readAllBytes());

                TaskPutDTO input = JsonUtils.deserialize(body, TaskPutDTO.class);

                if (input.title() == null || input.title().isEmpty()) {
                    TaskErrorDTO error = new TaskErrorDTO("title", "Title should not be empty.");
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                }

                if (input.title() != null && input.title().length() > 50) {
                    TaskErrorDTO error = new TaskErrorDTO("title", "Title should not exceed 50 characters.");
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                }

                if ( input.description() != null && input.description().length() > 255 ) {
                    TaskErrorDTO error = new TaskErrorDTO("description", "Description should not exceed 255 characters.");
                    sendResponse(exchange, 400, JsonUtils.serialize(error));
                }

                this.taskService.updateTaskById(id, input.title(), input.description(), input.done());
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
