package app.beautyminder.dto.todo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateTaskRequest {
    private String todoId;
    private String timeOfDay; // "morning" or "dinner"
    private int taskIndex; // Index of the task to update
    private String newTask; // New task content
}