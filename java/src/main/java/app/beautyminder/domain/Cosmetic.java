package app.beautyminder.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "cosmetics") // mongodb
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Cosmetic {

    @Id
    private String id;

    private String name;
    private String brand;

    @Setter
    private List<String> images = new ArrayList<>();

    private String glowpickUrl;

    private LocalDate expirationDate;
    private LocalDateTime createdAt;
    private LocalDate purchasedDate;
    private String category;
    private float averageRating = 0.0F; // ex) 3.14
    private int reviewCount = 0;
    private int totalRating = 0;

    @Builder.Default
    private List<String> keywords = new ArrayList<>();

//    @DBRef
//    private User user;

    @Builder
    public Cosmetic(String name, String brand, LocalDate expirationDate, LocalDate purchasedDate, String category) {
        this.name = name;
        this.brand = brand;
        this.expirationDate = expirationDate;
        this.purchasedDate = purchasedDate;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    public void updateAverageRating(int newRating) {
        this.reviewCount++;
        this.totalRating += newRating;
        this.averageRating = (float) this.totalRating / this.reviewCount;
        this.averageRating = Math.round(this.averageRating * 100.0) / 100.0f;  // Round to 2 decimal places
    }
}
