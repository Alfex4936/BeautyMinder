package app.beautyminder.controller.todo;

import app.beautyminder.domain.Todo;
import app.beautyminder.domain.User;
import app.beautyminder.dto.todo.AddTodoRequest;
import app.beautyminder.dto.todo.AddTodoResponse;
import app.beautyminder.dto.todo.UpdateTaskRequest;
import app.beautyminder.service.TodoService;
import app.beautyminder.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/todo")
public class TodoApiController {

    private final TodoService todoService;
    private final UserService userService;

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

    @PostMapping("/update")
    public ResponseEntity<AddTodoResponse> updateTodo(@RequestBody Todo todo) {
        try {
            Todo updatedTodo = todoService.updateTodo(todo);
            return ResponseEntity.ok(new AddTodoResponse("Todo updated successfully", updatedTodo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AddTodoResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTodo(@PathVariable String id) {
        try {
            todoService.deleteTodo(id);
            return ResponseEntity.ok("Todo deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update/task")
    public ResponseEntity<AddTodoResponse> updateSpecificTask(@RequestBody UpdateTaskRequest request) {
        try {
            Todo updatedTodo = todoService.updateSpecificTask(request.getTodoId(), request.getTimeOfDay(), request.getTaskIndex(), request.getNewTask());
            return ResponseEntity.ok(new AddTodoResponse("Task updated successfully", updatedTodo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AddTodoResponse(e.getMessage(), null));
        }
    }
}