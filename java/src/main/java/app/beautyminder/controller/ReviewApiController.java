package app.beautyminder.controller;

import app.beautyminder.domain.Review;
import app.beautyminder.dto.ReviewDTO;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.service.FileStorageService;
import app.beautyminder.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/review") // /review/api
public class ReviewApiController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorageService;

    @PostMapping("/new")
    public Review addReview(
            @RequestPart("review") ReviewDTO reviewDTO,
            @RequestPart("images") MultipartFile[] images) {

        Review review = Review.builder()
                .title(reviewDTO.getTitle())
                .content(reviewDTO.getContent())
                .rating(reviewDTO.getRating())
                .build();
        return reviewService.addReview(review, images);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Review> updateReview(
            @PathVariable("id") String id,
            @RequestBody Review reviewDetails,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        try {
            Review updatedReview = reviewService.updateReview(id, reviewDetails, images);
            return ResponseEntity.ok(updatedReview);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/image")
    public ResponseEntity<Resource> loadImage(@RequestParam("filename") String filename) {
        Resource file = fileStorageService.loadFile(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }

}
