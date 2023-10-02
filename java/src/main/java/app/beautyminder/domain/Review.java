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

    @Builder
    public Review(String content, Integer rating) {
        this.content = content;
        this.rating = rating;
    }

    public void update(Review reviewDetails) {
        // Update the fields of this Review instance with the values from reviewDetails
        this.content = reviewDetails.getContent();
        this.rating = reviewDetails.getRating();

        // Optionally, you might also want to update the images, user, and cosmetic fields
        // if they are part of what can be updated in a review.
        // For images, you might want to clear the current list and add all images from reviewDetails,
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