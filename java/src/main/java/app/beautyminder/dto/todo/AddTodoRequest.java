package app.beautyminder.dto.todo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
public class AddTodoRequest {
    private String userId;
    private LocalDate date;
    private List<String> morningTasks;
    private List<String> dinnerTasks;
}