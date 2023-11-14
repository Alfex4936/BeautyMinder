package app.beautyminder.dto;

import com.mongodb.lang.Nullable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDTO {
    private String content;
    private Integer rating;
    @Nullable
    private String cosmeticId;
}