package app.beautyminder.dto;

import app.beautyminder.domain.Diary;
import lombok.Getter;

@Getter
public class DiaryListViewResponse {

    private final Long id;
    private final String title;
    private final String content;

    public DiaryListViewResponse(Diary diary) {
        this.id = diary.getId();
        this.title = diary.getTitle();
        this.content = diary.getContent();
    }
}
