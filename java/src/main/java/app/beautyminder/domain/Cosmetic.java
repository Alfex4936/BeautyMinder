package app.beautyminder.domain;

import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "cosmetics") // mongodb
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Cosmetic {

    private static final Logger LOG = LoggerFactory
            .getLogger(Cosmetic.class);

    @Id
    private String id;

    private String name;

    @Setter
    private List<String> images = new ArrayList<>();

    private String glowpick_url;

    private LocalDate expirationDate;
    private LocalDateTime createdAt;
    private LocalDate purchasedDate;
    private Category category;
    private Status status;

    private float averageRating; // ex) 3.14
    private int reviewCount = 0;
    private int totalRating = 0;

//    @Setter
    private final List<String> keywords = new ArrayList<>();

//    @DBRef
//    private User user;

    @Builder
    public Cosmetic(String name, LocalDate expirationDate, LocalDate purchasedDate, Category category, Status status) {
        this.name = name;
        this.expirationDate = expirationDate;
        this.purchasedDate = purchasedDate;
        this.category = category;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public enum Category {
        스킨케어, 클렌징_필링, 마스크_팩, 선케어, 베이스, 아이, 립, 바디, 헤어, 네일, 향수, 기타
    }

    public enum Status {
        개봉, 미개봉
    }

    public void updateAverageRating(int newRating) {
        this.reviewCount++;
        this.totalRating += newRating;
        this.averageRating = (float) this.totalRating / this.reviewCount;
        this.averageRating = Math.round(this.averageRating * 100.0) / 100.0f;  // Round to 2 decimal places
    }
}
