package app.beautyminder.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "cosmetics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Cosmetic {

    @Id
    private String id;

    private String name;

    @Setter
    private List<String> images = new ArrayList<>();

    private LocalDate expirationDate;
    private LocalDateTime createdDate;
    private LocalDate purchasedDate;
    private Category category;
    private Status status;

    @DBRef
    private User user;

    @Builder
    public Cosmetic(String name, LocalDate expirationDate, LocalDate purchasedDate, Category category, Status status, User user) {
        this.name = name;
        this.expirationDate = expirationDate;
        this.purchasedDate = purchasedDate;
        this.category = category;
        this.status = status;
        this.user = user;
        this.createdDate = LocalDateTime.now();
    }

    public enum Category {
        스킨케어, 클렌징_필링, 마스크_팩, 선케어, 베이스, 아이, 립, 바디, 헤어, 네일, 향수, 기타
    }

    public enum Status {
        개봉, 미개봉
    }
}
