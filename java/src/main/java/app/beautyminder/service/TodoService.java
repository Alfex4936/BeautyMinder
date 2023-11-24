package app.beautyminder.service;

import app.beautyminder.domain.Todo;
import app.beautyminder.domain.TodoTask;
import app.beautyminder.dto.todo.AddTodoResponse;
import app.beautyminder.dto.todo.TaskUpdateDTO;
import app.beautyminder.dto.todo.TodoUpdateDTO;
import app.beautyminder.repository.TodoRepository;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TodoService {

    private final TodoRepository todoRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    private final MongoService mongoService;

    public Todo createTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    public void deleteTodo(String id) {
        todoRepository.deleteById(id);
    }

    public List<Todo> findTodosByUserId(String userId) {
        return todoRepository.findByUserId(userId);
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

    public boolean existsByDateAndUserId(LocalDate date, String id) {
        return todoRepository.existsByDateAndUserId(date, id);
    }

    public boolean existsByTodoIdAndUserId(String todoId, String userId) {
        return mongoService.existsWithReference(Todo.class, todoId, "user", userId);
    }

    public boolean checkUserAuthorizationForTodo(String todoId, String userId) {
        if (!mongoService.existsWithReference(Todo.class, todoId, "user", userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seems like the user doesn't have that todo");
        }
        return true;
    }

    public Optional<Todo> updateTodoTasks(String todoId, TodoUpdateDTO todoUpdateDTO) {
        return Optional.ofNullable(mongoTemplate.findOne(Query.query(Criteria.where("id").is(todoId)), Todo.class))
                .map(todo -> {
                    // Handle task deletions
                    Optional.ofNullable(todoUpdateDTO.getTaskIdsToDelete())
                            .ifPresent(idsToDelete -> todo.getTasks().removeIf(task -> idsToDelete.contains(task.getTaskId())));

                    // Handle task updates
                    Optional.ofNullable(todoUpdateDTO.getTasksToUpdate())
                            .ifPresent(updates -> updates.forEach(update -> updateTaskWithChanges(todo, update)));

                    // Handle adding new tasks
                    Optional.ofNullable(todoUpdateDTO.getTasksToAdd())
                            .ifPresent(tasksToAdd -> tasksToAdd.forEach(add ->
                                    todo.getTasks().add(new TodoTask(UUID.randomUUID().toString(), add.getDescription(), add.getCategory(), false))));

                    if (todo.getTasks().isEmpty()) {
                        // If there are no tasks left, remove the Todo
                        mongoTemplate.remove(todo);
                        return null;
                        // throw new ResponseStatusException(HttpStatus.OK, "Todo updated but has no tasks, got deleted");
                        // Return null or an appropriate response to indicate the Todo was removed
                    } else {
                        // Save the updated Todo
                        mongoTemplate.save(todo);
                        return todo;
                    }
                });
    }

    private void updateTaskWithChanges(Todo todo, TaskUpdateDTO update) {
        todo.getTasks().stream()
                .filter(task -> task.getTaskId().equals(update.getTaskId()))
                .findFirst()
                .ifPresent(taskToUpdate -> {
                    Optional.ofNullable(update.getDescription()).ifPresent(taskToUpdate::setDescription);
                    Optional.ofNullable(update.getCategory()).ifPresent(taskToUpdate::setCategory);
                    Optional.ofNullable(update.getIsDone()).ifPresent(taskToUpdate::setDone);
                });
    }

    public boolean deleteTodoById(String id) {
        DeleteResult result = mongoTemplate.remove(Query.query(Criteria.where("id").is(id)), Todo.class);
        return result.getDeletedCount() > 0;
    }

    public boolean deleteTaskFromTodoById(String todoId, String taskId) {
        Todo todo = mongoTemplate.findOne(Query.query(Criteria.where("id").is(todoId)), Todo.class);
        if (todo == null) {
            return false;
        }

        boolean removed = todo.getTasks().removeIf(task -> task.getTaskId().equals(taskId));
        if (removed) {
            mongoTemplate.save(todo);
        }
        return removed;
    }
}