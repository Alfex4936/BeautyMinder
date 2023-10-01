package app.beautyminder.service;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorageService;

    public Review addReview(Review review, MultipartFile[] images) {
        // Ensure images list is initialized
        if (review.getImages() == null) {
            review.setImages(new ArrayList<>());
        }

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
            throw new IllegalArgumentException("Review not found with id: " + id);
        }
    }


    public List<Review> getAllReviewsByCosmetic(Cosmetic cosmetic) {
        return reviewRepository.findByCosmetic(cosmetic);
    }

    public Optional<Review> getReviewById(String id) {
        return reviewRepository.findById(id);
    }
}