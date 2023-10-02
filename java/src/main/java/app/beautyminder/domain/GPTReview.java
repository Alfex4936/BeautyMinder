package app.beautyminder.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Document(collection = "gpt_review")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GPTReview {

    @Id
    private String id;

    @Setter
    private String positive; // positive review summary
    @Setter
    private String negative; // negative review summary
    private String gptVersion;

    @CreatedDate
    private LocalDateTime createdAt;

    @DBRef
    private Cosmetic cosmetic;

    @Builder
    public GPTReview(String positive, String negative, String gptVersion, Cosmetic cosmetic) {
        this.positive = positive;
        this.negative = negative;
        this.gptVersion = gptVersion;
        this.cosmetic = cosmetic;
    }
}