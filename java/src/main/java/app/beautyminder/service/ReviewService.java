package app.beautyminder.service;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.dto.PyReviewAnalysis;
import app.beautyminder.dto.ReviewDTO;
import app.beautyminder.dto.ReviewUpdateDTO;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.cosmetic.CosmeticService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorageService;
    private final CosmeticService cosmeticService;
    private final UserRepository userRepository;
    private final CosmeticRepository cosmeticRepository;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${server.python.review}")
    private String reviewProcessServer;

    public Optional<Review> findById(String id) {
        return reviewRepository.findById(id);
    }

    public List<Review> findAllByUser(User user) {
        return reviewRepository.findByUser(user);
    }

    public void deleteReview(String id) {
        var review = getReviewById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        var cosmetic = cosmeticService.findById(review.getCosmetic().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cosmetic not found"));

        // Update cosmetic's review score and save it
        cosmetic.removeRating(review.getRating());
        cosmeticRepository.save(cosmetic);

        // Delete the review by id
        reviewRepository.deleteById(id);
    }

    public Review createReview(ReviewDTO reviewDTO, MultipartFile[] images) throws JsonProcessingException {
        // Check if the user has already left a review for the cosmetic
        if (userHasReviewedCosmetic(reviewDTO.getUserId(), reviewDTO.getCosmeticId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has already reviewed this cosmetic.");
        }

        // Find the user and cosmetic, throwing exceptions if not found
        User user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        var cosmetic = cosmeticService.findById(reviewDTO.getCosmeticId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cosmetic not found"));

        // Create a new review from the DTO
        var review = Review.builder()
                .content(reviewDTO.getContent())
                .rating(reviewDTO.getRating())
                .user(user)
                .cosmetic(cosmetic)
                .isFiltered(false)
                .images(new ArrayList<>()) // Ensure the image list is initialized
                .build();

        // Update cosmetic's review score and save it
        cosmetic.increaseTotalCount();
        cosmetic.updateAverageRating(0, review.getRating());
        cosmeticRepository.save(cosmetic);

        // Store images and set image URLs in review
        if (images != null) {
            for (var image : images) {
                var imageUrl = fileStorageService.storeFile(image, "review/");
                review.getImages().add(imageUrl);
            }
        }

        // Python NLP work
        // TODO: call in background or process before saving
//        String reviewJson = objectMapper.writeValueAsString(review);
//        callProcessAPI(reviewJson);

        // Save the review
        return reviewRepository.save(review);
    }

    private void callProcessAPI(String reviewJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request entity
        HttpEntity<String> request = new HttpEntity<>(reviewJson, headers);

        // Send POST request
        ResponseEntity<PyReviewAnalysis> response = restTemplate.postForEntity(reviewProcessServer, request, PyReviewAnalysis.class);

        log.info("BEMINDER: python server: {}", response.getBody().toString());
    }


    public Optional<Review> updateReview(String revId, ReviewUpdateDTO reviewUpdateDetails, MultipartFile[] images) {
        var query = new Query(Criteria.where("id").is(revId));
        var review = mongoTemplate.findOne(query, Review.class);

        if (review != null) {
            int oldRating = review.getRating(); // Store the old rating

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
                // Update the cosmetic's average rating
                var cosmetic = review.getCosmetic();
                cosmetic.updateAverageRating(oldRating, review.getRating()); // Update with old and new ratings
                cosmeticRepository.save(cosmetic);
            }

            // Handle image upload
            if (images != null) {
                for (var image : images) {
                    var originalFilename = image.getOriginalFilename();
                    if (originalFilename != null) {
                        // Remove the old image if it exists
                        review.getImages().removeIf(img -> img.contains(originalFilename));
                        // Store the new image and add its URL to the review
                        var imageUrl = fileStorageService.storeFile(image, "review/");
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
        Pageable pageable = PageRequest.of(0, 10);
        return reviewRepository.findReviewsByRatingAndUserIds(minRating, userIds, pageable);
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
        query.limit(10);

        // Execute the query to fetch the filtered reviews
        return mongoTemplate.find(query, Review.class);
    }

    public boolean userHasReviewedCosmetic(String userId, String cosmeticId) {
        return reviewRepository.existsByUserIdAndCosmeticId(userId, cosmeticId);
    }
}