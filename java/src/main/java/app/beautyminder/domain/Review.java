package app.beautyminder.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Review {

    @Id
    private String id;

    private String content;
    private Integer rating;

    @Builder.Default
    private List<String> images = new ArrayList<>();

    @DBRef
    @Indexed
    private User user;

    @DBRef
    @Indexed
    private Cosmetic cosmetic;

    @CreatedDate
    private LocalDateTime createdAt;

    @Setter
    private boolean isFiltered = false; // Field for offensive content flag

    // Inner class to hold NLP analysis results
    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class NlpAnalysis {
        private double offensivenessProbability;
        private Map<String, Double> similarities;
    }

    // Field for NLP analysis results
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
}