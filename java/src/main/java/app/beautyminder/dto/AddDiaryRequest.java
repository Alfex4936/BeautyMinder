package app.beautyminder.dto;

import app.beautyminder.domain.Diary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddDiaryRequest {

    private String title;
    private String content;

    public Diary toEntity(String author) {
        return Diary.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();
    }
}

