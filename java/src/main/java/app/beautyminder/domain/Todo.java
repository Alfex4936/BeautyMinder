package app.beautyminder.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "todos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Todo {

    @Id
    private String id;

    private LocalDate date;
    private List<String> morningTasks;
    private List<String> dinnerTasks;

    @DBRef
    private User user;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Todo(LocalDate date, List<String> morningTasks, List<String> dinnerTasks, User user) {
        this.date = date;
        this.morningTasks = morningTasks;
        this.dinnerTasks = dinnerTasks;
        this.user = user;
    }
}