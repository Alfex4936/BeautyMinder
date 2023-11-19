package app.beautyminder.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "todos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@CompoundIndex(name = "idx_date_user", def = "{'date': 1, 'user.id': 1}", unique = true)
public class Todo {

    @Id
    private String id;

    @Indexed
    private LocalDate date;

    private List<TodoTask> tasks;

    private boolean isAllDone; // all tasks done status

    @DBRef
    @Indexed
    private User user;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Todo(LocalDate date, List<TodoTask> tasks, User user) {
        this.date = date;
        this.tasks = tasks;
        this.user = user;
    }
}