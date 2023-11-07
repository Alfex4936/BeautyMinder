package app.beautyminder.dto.todo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskUpdateDTO {
    private String taskId;
    private String description;
    private String category;
    private Boolean isDone;
}