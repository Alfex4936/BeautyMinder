package app.beautyminder.service;

import app.beautyminder.domain.PasswordResetToken;
import app.beautyminder.domain.Todo;
import app.beautyminder.domain.TodoTask;
import app.beautyminder.domain.User;
import app.beautyminder.dto.PasswordResetResponse;
import app.beautyminder.dto.sms.SmsResponseDTO;
import app.beautyminder.repository.TodoRepository;
import app.beautyminder.service.auth.SmsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private MongoService mongoService;

    @InjectMocks
    private TodoService todoService;

    private Todo createTodo() {
        var tasks = new TodoTask("taskId", "desc", "category", true);
        var todo = Todo.builder().tasks(new ArrayList<>(List.of(tasks))).build();
        ReflectionTestUtils.setField(todo, "id", "todoId");

        return todo;
    }

    @BeforeEach
    void setUp() {
        todoRepository = mock(TodoRepository.class);
        mongoTemplate = mock(MongoTemplate.class);
        mongoService = mock(MongoService.class);
        todoService = new TodoService(todoRepository, mongoService);
        ReflectionTestUtils.setField(todoService, "mongoTemplate", mongoTemplate);
    }


    @Test
    public void testCreateTodo() {
        Todo todo = createTodo();
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        Todo result = todoService.createTodo(todo);
        assertEquals(todo, result);
    }

    @Test
    public void testDeleteTodo() {
        doNothing().when(todoRepository).deleteById(anyString());

        todoService.deleteTodo("testId");
        verify(todoRepository).deleteById("testId");
    }

    @Test
    public void testFindTodosByUserId() {
        List<Todo> todos = List.of(createTodo());
        when(todoRepository.findByUserId(anyString())).thenReturn(todos);

        List<Todo> result = todoService.findTodosByUserId("userId");
        assertEquals(todos, result);
    }

    @Test
    public void testDeleteTodoById() {
        when(mongoTemplate.remove(any(Query.class), eq(Todo.class))).thenReturn(DeleteResult.acknowledged(1L));

        boolean result = todoService.deleteTodoById("todoId");
        assertTrue(result);
    }

    @Test
    public void testDeleteTaskFromTodoById() {
        Todo todo = createTodo();
        when(mongoTemplate.findOne(any(Query.class), eq(Todo.class))).thenReturn(todo);
        when(mongoTemplate.save(any(Todo.class))).thenReturn(todo);

        boolean result = todoService.deleteTaskFromTodoById("todoId", "taskId");
        assertTrue(result);
    }

}