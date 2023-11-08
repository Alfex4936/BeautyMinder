package app.beautyminder.service;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.dto.ReviewDTO;
import app.beautyminder.dto.ReviewUpdateDTO;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorageService;
    private final CosmeticService cosmeticService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CosmeticRepository cosmeticRepository;
    private final MongoTemplate mongoTemplate;

    public Optional<Review> findById(String id) {
        return reviewRepository.findById(id);
    }

    public void deleteReview(String id) {
        Review review = getReviewById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        Cosmetic cosmetic = cosmeticService.findById(review.getCosmetic().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cosmetic not found"));

        // Update cosmetic's review score and save it
        cosmetic.updateAverageRating(-review.getRating(), false);
        cosmeticRepository.save(cosmetic);

        // Delete the review by id
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
        cosmetic.updateAverageRating(review.getRating(), true);
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

            // Inside the updateReview method where you handle the image upload
            if (images != null) {
                for (MultipartFile image : images) {
                    String originalFilename = image.getOriginalFilename();
                    if (originalFilename != null) {
                        // Remove the old image if it exists
                        review.getImages().removeIf(img -> img.contains(originalFilename));
                        // Store the new image and add its URL to the review
                        String imageUrl = fileStorageService.storeFile(image);
                        review.getImages().add(imageUrl);
                    }
                }
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
        // First, find all users with the specified Baumann skin type.
        List<User> usersWithBaumann = userRepository.findByBaumann(userBaumann);

        // Extract the IDs from the users and convert them to ObjectId instances.
        List<ObjectId> userIds = usersWithBaumann.stream()
                .map(user -> new ObjectId(user.getId()))
                .collect(Collectors.toList());

        // Now, find all reviews that have a minimum rating and whose user ID is in the list of user IDs.
        return reviewRepository.findReviewsByRatingAndUserIds(minRating, userIds);
    }

    public List<Review> getReviewsForRecommendation(Integer minRating, String userBaumann) {
        // Split the user's Baumann skin type into individual characters
        String[] baumannTypes = userBaumann.split("");

        // Initialize the criteria for the rating
        Criteria ratingCriteria = Criteria.where("rating").gte(minRating);

        // List to hold combined criteria for any two Baumann types
        List<Criteria> combinedBaumannCriteria = new ArrayList<>();

        // Combine each Baumann type with every other type to check for any two matches
        for (int i = 0; i < baumannTypes.length; i++) {
            for (int j = i + 1; j < baumannTypes.length; j++) {
                Criteria firstTypeCriteria = Criteria.where("nlpAnalysis." + baumannTypes[i]).gt(Double.valueOf("0.5"));
                Criteria secondTypeCriteria = Criteria.where("nlpAnalysis." + baumannTypes[j]).gt(Double.valueOf("0.5"));
                combinedBaumannCriteria.add(new Criteria().andOperator(firstTypeCriteria, secondTypeCriteria));
            }
        }

        // Create the final criteria using 'or' operator to combine all two-type matches
        Criteria finalCriteria = ratingCriteria.andOperator(new Criteria().orOperator(combinedBaumannCriteria.toArray(new Criteria[0])));

        // Create the query using the final criteria
        Query query = new Query(finalCriteria);

        // Execute the query to fetch the filtered reviews
        return mongoTemplate.find(query, Review.class);
    }

    public boolean userHasReviewedCosmetic(String userId, String cosmeticId) {
        return reviewRepository.existsByUserIdAndCosmeticId(userId, cosmeticId);
    }
}