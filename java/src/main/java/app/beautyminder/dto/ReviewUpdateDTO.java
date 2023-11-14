package app.beautyminder.dto;

import com.mongodb.lang.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReviewUpdateDTO {
    private String content;
    private Integer rating;
    @Nullable
    private List<String> imagesToDelete; // URLs or IDs of images to delete
}