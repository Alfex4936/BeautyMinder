package app.beautyminder.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "todos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Todo {

    @Id
    private String id;

    private LocalDate date;
    private List<String> morningTasks;
    private List<String> dinnerTasks;

    @DBRef
    private User user;

    @Builder
    public Todo(LocalDate date, List<String> morningTasks, List<String> dinnerTasks, User user) {
        this.date = date;
        this.morningTasks = morningTasks;
        this.dinnerTasks = dinnerTasks;
        this.user = user;
    }
}