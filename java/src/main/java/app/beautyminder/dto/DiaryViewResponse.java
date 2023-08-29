package app.beautyminder.dto;

import app.beautyminder.domain.Diary;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class DiaryViewResponse {

  private Long id;
  private String title;
  private String content;
  private LocalDateTime createdAt;
  private String author;

  public DiaryViewResponse(Diary diary) {
    this.id = diary.getId();
    this.title = diary.getTitle();
    this.content = diary.getContent();
    this.createdAt = diary.getCreatedAt();
    this.author = diary.getAuthor();
  }
}
