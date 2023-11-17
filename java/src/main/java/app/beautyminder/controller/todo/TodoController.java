package app.beautyminder.controller.todo;

import app.beautyminder.domain.Todo;
import app.beautyminder.domain.TodoTask;
import app.beautyminder.domain.User;
import app.beautyminder.dto.todo.AddTodoRequest;
import app.beautyminder.dto.todo.AddTodoResponse;
import app.beautyminder.dto.todo.TodoUpdateDTO;
import app.beautyminder.service.MongoService;
import app.beautyminder.service.TodoService;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.util.AuthenticatedUser;
import app.beautyminder.util.ValidUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/todo")
@PreAuthorize("hasRole('ROLE_USER')")
public class TodoController {

    private final TodoService todoService;
    private final MongoService mongoService;

    @Operation(summary = "Retrieve all todos", description = "모든 Todo 항목을 검색합니다. [User 권한 필요]", tags = {"Todo Operations"})
    @GetMapping("/all")
    public Map<String, Object> getTodos(@Parameter(hidden = true) @AuthenticatedUser User user) {
        List<Todo> existingTodos = todoService.findTodosByUserId(user.getId());
        return createResponse("Here are the todos", existingTodos.isEmpty() ? Collections.emptyList() : existingTodos);
    }

    @Operation(summary = "Create a new todo", description = "새로운 Todo 항목을 추가합니다. [User 권한 필요]", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Todo details for creation"), tags = {"Todo Operations"})
    @PostMapping("/create")
    public ResponseEntity<AddTodoResponse> createTodo(@RequestBody AddTodoRequest request, @Parameter(hidden = true) @AuthenticatedUser User user) {
        try {
            // Check if a Todo already exists for this user with the same date
            if (todoService.existsByDateAndUserId(request.getDate(), user.getId())) {
                return ResponseEntity.badRequest().body(new AddTodoResponse("Todo already exists for this date", null));
            }

            var tasks = request.getTasks().stream()
                    .map(taskDto -> new TodoTask(UUID.randomUUID().toString(), taskDto.getDescription(), taskDto.getCategory(), false))
                    .collect(Collectors.toList());

            var todo = new Todo(request.getDate(), tasks, user);
            var savedTodo = todoService.createTodo(todo);

            return ResponseEntity.ok(new AddTodoResponse("Todo added successfully", savedTodo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AddTodoResponse(e.getMessage(), null));
        }
    }

    @Operation(summary = "Update an existing todo by fields", description = "기존 Todo 항목을 업데이트합니다. (DB call 방식) [User 권한 필요]", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Todo details for update"), tags = {"Todo Operations"})
    @PutMapping("/update/fields/{todoId}")
    public ResponseEntity<AddTodoResponse> updateTodoByFields(@PathVariable String todoId, @RequestBody Map<String, Object> updates, @Parameter(hidden = true) @AuthenticatedUser User user) {
        todoService.checkUserAuthorizationForTodo(todoId, user.getId());

        Optional<Todo> updatedTodo = mongoService.updateFields(todoId, updates, Todo.class);

        return updatedTodo.map(todo -> ResponseEntity.ok(new AddTodoResponse("Updated Todo", todo))).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AddTodoResponse("Failed to update Todo", null)));
    }

    @Operation(summary = "Update an existing todo", description = "기존 Todo 항목을 업데이트합니다. (JSON 방식) [User 권한 필요]", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Todo update"), tags = {"Todo Operations"})
    @PutMapping("/update/{todoId}")
    public ResponseEntity<AddTodoResponse> updateTodoByTask(@PathVariable String todoId, @RequestBody TodoUpdateDTO todoUpdateDTO, @Parameter(hidden = true) @AuthenticatedUser User user) {
        todoService.checkUserAuthorizationForTodo(todoId, user.getId());
        try {
            Optional<Todo> updatedTodo = todoService.updateTodoTasks(todoId, todoUpdateDTO);

            return updatedTodo.map(todo -> ResponseEntity.ok(new AddTodoResponse("Updated Todo", todo))).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AddTodoResponse("Failed to update Todo", null)));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Delete a todo", description = "Todo 항목을 삭제합니다. [User 권한 필요]", tags = {"Todo Operations"})
    @DeleteMapping("/delete/{todoId}")
    public ResponseEntity<String> deleteTodo(@PathVariable String todoId, @Parameter(hidden = true) @AuthenticatedUser User user) {
        todoService.checkUserAuthorizationForTodo(todoId, user.getId());

        try {
            boolean deleted = todoService.deleteTodoById(todoId);
            if (deleted) {
                return ResponseEntity.ok("Todo deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Todo not found with id: " + todoId);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Delete a task", description = "Task를 삭제합니다. (USE /delete/id) [User 권한 필요]", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Todo update"), tags = {"Todo Operations"})
    @DeleteMapping("/delete/{todoId}/task/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable String todoId, @PathVariable String taskId, @Parameter(hidden = true) @AuthenticatedUser User user) {
        todoService.checkUserAuthorizationForTodo(todoId, user.getId());

        try {
            boolean taskDeleted = todoService.deleteTaskFromTodoById(todoId, taskId);
            if (taskDeleted) {
                return ResponseEntity.ok("Task deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task or Todo not found");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private Map<String, Object> createResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("todos", data);
        return response;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleUserNotFound(IllegalArgumentException e) {
        return createResponse("No such user: " + e.getCause(), Collections.emptyList());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AddTodoResponse handleException(Exception e) {
        return new AddTodoResponse(e.getMessage(), null);
    }
}