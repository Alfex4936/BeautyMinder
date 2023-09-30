package app.beautyminder.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDTO {
    private String title;
    private String content;
    private Integer rating;
}