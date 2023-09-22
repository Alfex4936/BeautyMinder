package app.beautyminder.service;

import app.beautyminder.domain.Todo;
import app.beautyminder.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public Todo createTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    public Todo updateTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    public void deleteTodo(String id) {
        todoRepository.deleteById(id);
    }

    public List<Todo> findTodosByUserId(String userId) {
        return todoRepository.findByUserId(userId);
    }

    public List<Todo> findTodosByDate(LocalDate date) {
        return todoRepository.findByDate(date);
    }

    public List<Todo> findTodosByUserIdAndDate(String userId, LocalDate date) {
        return todoRepository.findByUserIdAndDate(userId, date);
    }

    public List<Todo> findTodosBetweenDates(LocalDate startDate, LocalDate endDate) {
        return todoRepository.findBetweenDates(startDate, endDate);
    }

    public List<Todo> findTodosBetweenDatesByUserId(String userId, LocalDate startDate, LocalDate endDate) {
        return todoRepository.findBetweenDatesByUserId(userId, startDate, endDate);
    }

    public List<Todo> findTodosByTaskKeyword(String keyword) {
        return todoRepository.findByTaskKeyword(keyword);
    }

    public List<Todo> findTodosByTaskKeywordAndUserId(String userId, String keyword) {
        return todoRepository.findByTaskKeywordAndUserId(userId, keyword);
    }

    public Todo updateSpecificTask(String todoId, String timeOfDay, int taskIndex, String newTask) {
        Optional<Todo> optionalTodo = todoRepository.findById(todoId);
        if (optionalTodo.isPresent()) {
            Todo todo = optionalTodo.get();
            List<String> tasks = "morning".equalsIgnoreCase(timeOfDay) ? todo.getMorningTasks() : todo.getDinnerTasks();
            if (taskIndex >= 0 && taskIndex < tasks.size()) {
                tasks.set(taskIndex, newTask);
                return todoRepository.save(todo);
            } else {
                throw new IllegalArgumentException("Invalid task index");
            }
        } else {
            throw new IllegalArgumentException("Todo not found");
        }
    }
}