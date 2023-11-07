package app.beautyminder.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TodoTask {
    private String taskId;
    private String description;
    private String category; // e.g., "morning", "dinner"
    private boolean isDone;
}