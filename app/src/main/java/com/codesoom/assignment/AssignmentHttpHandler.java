package com.codesoom.assignment;

import com.codesoom.assignment.models.Task;
import com.codesoom.assignment.repository.TaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssignmentHttpHandler implements HttpHandler {
    private TaskRepository taskRepository = TaskRepository.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Task> tasks = new ArrayList<>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        InputStream inputStream = exchange.getRequestBody();
        String body = new BufferedReader(new InputStreamReader(inputStream))
                        .lines()
                        .collect(Collectors.joining("\n"));

        // id값 추출 예) tasks/1 -> 1
        String taskId = path.split("/")[1];

        System.out.println(method + " " + path);
        // body 없는 경우 넘어가기
        if (!body.isBlank()) {
            // test object 확인
            Task task = toTask(body);
            System.out.println(task);
            System.out.println(body);
        }

        String content = "Hello, world!";

        if (method.equals("GET") && path.equals("/tasks")) {
            List<Task> findTasks = taskRepository.findAll();
            sendResponse(exchange, tasksToJson(findTasks), HttpStatus.OK);
            return;
        }

        if (method.equals("GET") && path.startsWith("/tasks/")) {
            Long id = Long.parseLong(path.split("/")[2]);
            Task findTask = taskRepository.findById(id);
            sendResponse(exchange, tasksToJson(findTask), HttpStatus.OK);
            return;
        }

        if (method.equals("POST") && path.equals("/tasks")) {
            Task task = toTask(body);
            Task savedTask = taskRepository.save(task);
            sendResponse(exchange, tasksToJson(savedTask), HttpStatus.Created);
            return;
        }

        if (method.equals("PUT") && path.equals("/tasks")) {
//            content = tasksToJson();
        }

        if (method.equals("DELETE") && path.equals("/tasks")) {
//            content = tasksToJson();
        }

    }

    private void sendResponse(HttpExchange exchange, String content, HttpStatus code) throws IOException {
        exchange.sendResponseHeaders(code.getHttpStatus(), content.getBytes().length);

        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(content.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    private Task toTask(String content) throws JsonProcessingException {
        return objectMapper.readValue(content, Task.class);
    }

    private String tasksToJson(Object obj) throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, obj);

        return outputStream.toString();
    }
}
