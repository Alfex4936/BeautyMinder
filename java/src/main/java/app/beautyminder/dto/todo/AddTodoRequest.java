package app.beautyminder.dto.todo;

import app.beautyminder.domain.TodoTask;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
public class AddTodoRequest {
    private LocalDate date;
    private List<TodoTask> tasks;
}