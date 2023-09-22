package app.beautyminder.dto.todo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
public class UpdateTaskRequest {
    private String todoId;
    private String timeOfDay; // "morning" or "dinner"
    private int taskIndex; // Index of the task to update
    private String newTask; // New task content
}