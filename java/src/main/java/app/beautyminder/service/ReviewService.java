package app.beautyminder.service;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.dto.ReviewDTO;
import app.beautyminder.dto.ReviewUpdateDTO;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorageService;
    private final CosmeticService cosmeticService;
    private final UserService userService;
    private final CosmeticRepository cosmeticRepository;
    private final MongoTemplate mongoTemplate;

    public Optional<Review> findById(String id) {
        return reviewRepository.findById(id);
    }

    public void deleteReview(String id) {
        reviewRepository.deleteById(id);
    }


    public Review createReview(ReviewDTO reviewDTO, MultipartFile[] images) {
        // Check if the user has already left a review for the cosmetic
        if (userHasReviewedCosmetic(reviewDTO.getUserId(), reviewDTO.getCosmeticId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has already reviewed this cosmetic.");
        }

        // Find the user and cosmetic, throwing exceptions if not found
        User user = userService.findById(reviewDTO.getUserId()); // This will throw an IllegalArgumentException if not found
        Cosmetic cosmetic = cosmeticService.findById(reviewDTO.getCosmeticId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cosmetic not found"));

        // Create a new review from the DTO
        Review review = Review.builder().content(reviewDTO.getContent()).rating(reviewDTO.getRating()).user(user).cosmetic(cosmetic).isFiltered(false).images(new ArrayList<>()) // Ensure the images list is initialized
                .build();

        // Update cosmetic's review score and save it
        cosmetic.updateAverageRating(review.getRating());
        cosmeticRepository.save(cosmetic);

        // Store images and set image URLs in review
        if (images != null) {
            for (MultipartFile image : images) {
                String imageUrl = fileStorageService.storeFile(image);
                review.getImages().add(imageUrl);
            }
        }

        // Save the review
        return reviewRepository.save(review);
    }

    public Optional<Review> updateReview(String revId, ReviewUpdateDTO reviewUpdateDetails, MultipartFile[] images) {
        // Find the existing review
        Query query = new Query(Criteria.where("id").is(revId));
        final Review review = mongoTemplate.findOne(query, Review.class);

        if (review != null) {

            // Handle deleted images
            if (reviewUpdateDetails.getImagesToDelete() != null) {
                reviewUpdateDetails.getImagesToDelete().forEach(imageId -> {
                    fileStorageService.deleteFile(imageId);
                    review.getImages().removeIf(img -> img.equals(imageId));
                });
            }

            // Update the fields from ReviewDTO
            if (reviewUpdateDetails.getContent() != null) {
                review.setContent(reviewUpdateDetails.getContent());
            }
            if (reviewUpdateDetails.getRating() != null) {
                review.setRating(reviewUpdateDetails.getRating());
            }

            // Handle the image upload
            if (images != null && images.length > 0) {
                Arrays.stream(images).forEach(image -> {
                    String imageUrl = fileStorageService.storeFile(image);
                    // Add the new image URL to the review's list
                    review.getImages().add(imageUrl);
                });
            }

            // Save the updated review
            mongoTemplate.save(review);

            return Optional.of(review);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found with id: " + revId);
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
        Stream.of(baumannTypes).forEach(type -> criteria.and("nlpAnalysis." + type).gt(0.5));

        // Create the query using the criteria
        Query query = new Query(criteria);

        // Execute the query to fetch the filtered reviews
        return mongoTemplate.find(query, Review.class);
    }

    public boolean userHasReviewedCosmetic(String userId, String cosmeticId) {
        return reviewRepository.existsByUserIdAndCosmeticId(userId, cosmeticId);
    }
}