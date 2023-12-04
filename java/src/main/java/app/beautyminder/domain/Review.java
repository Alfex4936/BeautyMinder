package app.beautyminder.domain;

import app.beautyminder.dto.NlpAnalysis;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Document(collection = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Review {

    @Id
    private String id;

    @Setter
    private String content;
    @Setter
    private Integer rating;

    @Builder.Default
    @Setter
    private List<String> images = new ArrayList<>();

    @DBRef
    @Indexed
    private User user;

    @DBRef
    @Indexed
    private Cosmetic cosmetic;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Setter
    @Builder.Default
//    @JsonProperty("isFiltered")
    private boolean isFiltered = false; // Field for offensive content flag, will become "filtered" in jsons

    // Field for NLP analysis results
    @Setter
    private NlpAnalysis nlpAnalysis;

    // Constructor using builder pattern for NlpAnalysis
    @Builder(builderMethodName = "reviewBuilder")
    public Review(String content, Integer rating, List<String> images, User user, Cosmetic cosmetic, boolean isFiltered, NlpAnalysis nlpAnalysis) {
        this.content = content;
        this.rating = rating;
        this.images = images;
        this.user = user;
        this.cosmetic = cosmetic;
        this.isFiltered = isFiltered;
        this.nlpAnalysis = nlpAnalysis;
    }

    @Builder
    public Review(String content, Integer rating) {
        this.content = content;
        this.rating = rating;
        this.isFiltered = false;
    }

    public void update(Review reviewDetails) {
        // Update the fields of this Review instance with the values from reviewDetails
        this.content = reviewDetails.getContent();
        this.rating = reviewDetails.getRating();

        // Optionally, might also want to update the images, user, and cosmetic fields
        // if they are part of what can be updated in a review.
        // For images, might want to clear the current list and add all images from reviewDetails,
        // or perhaps just append the new images to the current list.
        this.images.clear();
        this.images.addAll(reviewDetails.getImages());

        // For user and cosmetic, simply update the fields if the values in reviewDetails are non-null
        if (reviewDetails.getUser() != null) {
            this.user = reviewDetails.getUser();
        }
        if (reviewDetails.getCosmetic() != null) {
            this.cosmetic = reviewDetails.getCosmetic();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Inner class to hold NLP analysis results
//    @Getter
//    @Setter
//    @NoArgsConstructor(access = AccessLevel.PROTECTED)
//    @AllArgsConstructor
//    @Builder
//    public static class NlpAnalysis {
//        private double offensivenessProbability;
//
//        // Change Map to List of Similarity objects
//        @Builder.Default
//        private List<Similarity> similarities = new ArrayList<>();
//
//        @Getter
//        @Setter
//        @NoArgsConstructor(access = AccessLevel.PROTECTED)
//        @AllArgsConstructor
//        @Builder
//        public static class Similarity {
//            private String key;
//            private Double value;
//        }
//    }

}