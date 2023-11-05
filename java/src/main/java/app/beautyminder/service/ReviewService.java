package app.beautyminder.service;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorageService;
    private final CosmeticRepository cosmeticRepository;
    private final MongoTemplate mongoTemplate;

    public Review addReview(Review review, MultipartFile[] images) {
        // Ensure images list is initialized
        review.getCosmetic().updateAverageRating(review.getRating()); // update cosmetic's review score

        // HERE mongodb doesn't support cascading saves via `@DBRef`
        // Save the updated Cosmetic object
        cosmeticRepository.save(review.getCosmetic());

        // Store images and set image URLs in review
        for (MultipartFile image : images) {
            String imageUrl = fileStorageService.storeFile(image);
            review.getImages().add(imageUrl);
        }
        return reviewRepository.save(review);
    }

    public Review updateReview(String id, Review reviewDetails, MultipartFile[] images) {
        Optional<Review> optionalReview = reviewRepository.findById(id);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.update(reviewDetails);
            if (images != null) {
                for (MultipartFile image : images) {
                    String imageUrl = fileStorageService.storeFile(image);
                    review.getImages().add(imageUrl);
                }
            }
            return reviewRepository.save(review);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found with id: " + id);
        }
    }


    public List<Review> getAllReviewsByCosmetic(Cosmetic cosmetic) {
        return reviewRepository.findByCosmetic(cosmetic);
    }

    public Optional<Review> getReviewById(String id) {
        return reviewRepository.findById(id);
    }

    public List<Review> getReviewsOfBaumann(Integer minRating, String userBaumann) {
        return reviewRepository.findReviewsByRatingAndUserBaumann(minRating, userBaumann);
    }

    // Method to get reviews based on Baumann skin type and probability greater than 0.5
    public List<Review> getReviewsForRecommendation(Integer minRating, String userBaumann) {
        // Split the user's Baumann skin type into individual characters
        String[] baumannTypes = userBaumann.split("");

        // Build the criteria for each Baumann type with probability > 0.5
        Criteria criteria = Criteria.where("rating").gte(minRating);
        Stream.of(baumannTypes).forEach(type ->
                criteria.and("nlpAnalysis." + type).gt(0.5)
        );

        // Create the query using the criteria
        Query query = new Query(criteria);

        // Execute the query to fetch the filtered reviews
        return mongoTemplate.find(query, Review.class);
    }
}