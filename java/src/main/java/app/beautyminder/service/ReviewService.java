package app.beautyminder.service;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorageService;
    private final CosmeticRepository cosmeticRepository;

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

    public List<Review> getReviewsForRecommendation(Integer minRating, String userBaumann) {
        return reviewRepository.findReviewsByRatingAndUserBaumann(minRating, userBaumann);
    }
}