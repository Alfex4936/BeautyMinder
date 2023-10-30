package app.beautyminder.controller.todo;

import app.beautyminder.domain.Todo;
import app.beautyminder.domain.User;
import app.beautyminder.dto.todo.AddTodoRequest;
import app.beautyminder.dto.todo.AddTodoResponse;
import app.beautyminder.dto.todo.UpdateTaskRequest;
import app.beautyminder.service.TodoService;
import app.beautyminder.service.auth.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/todo")
public class TodoController {

    private final TodoService todoService;
    private final UserService userService;

    @Operation(
            summary = "Retrieve all todos",
            description = "모든 Todo 항목을 검색합니다.",
            tags = {"Todo Operations"}
    )
    @GetMapping("/all")
    public Map<String, Object> getTodos(@RequestParam("userId") String userId) {
        User user = userService.findById(userId);
        List<Todo> existingTodos = todoService.findTodosByUserId(user.getId());
        return createResponse("Here are the todos", existingTodos.isEmpty() ? Collections.emptyList() : existingTodos);
    }

    @Operation(
            summary = "Add a new todo",
            description = "새로운 Todo 항목을 추가합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Todo details for creation"),
            tags = {"Todo Operations"}
    )
    @PostMapping("/add")
    public ResponseEntity<AddTodoResponse> addTodo(@RequestBody AddTodoRequest request) {
        try {
            User user = userService.findById(request.getUserId());

            // Check if a Todo already exists for this user with the same date
            List<Todo> existingTodos = todoService.findTodosByUserIdAndDate(user.getId(), request.getDate());
            if (!existingTodos.isEmpty()) {
                return ResponseEntity.badRequest().body(new AddTodoResponse("Todo already exists for this date", null));
            }

            Todo todo = Todo.builder()
                    .date(request.getDate())
                    .morningTasks(request.getMorningTasks())
                    .dinnerTasks(request.getDinnerTasks())
                    .user(user)
                    .build();

            Todo savedTodo = todoService.createTodo(todo);
            return ResponseEntity.ok(new AddTodoResponse("Todo added successfully", savedTodo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AddTodoResponse(e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Update an existing todo",
            description = "기존 Todo 항목을 업데이트합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Todo details for update"),
            tags = {"Todo Operations"}
    )
    @PutMapping("/update")
    public ResponseEntity<AddTodoResponse> updateTodo(@RequestBody Todo todo) {
        try {
            Todo updatedTodo = todoService.updateTodo(todo);
            return ResponseEntity.ok(new AddTodoResponse("Todo updated successfully", updatedTodo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AddTodoResponse(e.getMessage(), null));
        }
    }

    @Operation(
            summary = "Delete a todo",
            description = "Todo 항목을 삭제합니다.",
            tags = {"Todo Operations"}
    )
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTodo(@PathVariable String id) {
        try {
            todoService.deleteTodo(id);
            return ResponseEntity.ok("Todo deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Update a specific task within a todo",
            description = "Todo 항목 내의 특정 작업을 업데이트합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Task update details"),
            tags = {"Todo Operations"}
    )
    @PostMapping("/update/task")
    public ResponseEntity<AddTodoResponse> updateSpecificTask(@RequestBody UpdateTaskRequest request) {
        try {
            Todo updatedTodo = todoService.updateSpecificTask(request.getTodoId(), request.getTimeOfDay(), request.getTaskIndex(), request.getNewTask());
            return ResponseEntity.ok(new AddTodoResponse("Task updated successfully", updatedTodo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AddTodoResponse(e.getMessage(), null));
        }
    }

    private Map<String, Object> createResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("msg", message);
        response.put("todos", data);
        return response;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleUserNotFound(IllegalArgumentException e) {
        return createResponse("No such user", Collections.emptyList());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AddTodoResponse handleException(Exception e) {
        return new AddTodoResponse(e.getMessage(), null);
    }
}