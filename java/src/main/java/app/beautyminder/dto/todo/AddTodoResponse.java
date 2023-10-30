package app.beautyminder.dto.todo;

import app.beautyminder.domain.Todo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddTodoResponse {
    private String message;
    private Todo todo;

    public AddTodoResponse(String message, Todo todo) {
        this.message = message;
        this.todo = todo;
    }
}