package app.beautyminder.dto.todo;

import com.mongodb.lang.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class TodoUpdateDTO {
    @org.springframework.lang.Nullable
    private List<TaskUpdateDTO> tasksToUpdate;
    @org.springframework.lang.Nullable
    private List<TaskUpdateDTO> tasksToAdd;
    @org.springframework.lang.Nullable
    private List<String> taskIdsToDelete;
}

