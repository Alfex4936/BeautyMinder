package app.beautyminder.controller.review;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.dto.ReviewDTO;
import app.beautyminder.service.FileStorageService;
import app.beautyminder.service.ReviewService;
import app.beautyminder.service.cosmetic.CosmeticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final FileStorageService fileStorageService;
    private final CosmeticService cosmeticService;

    // Retrieve all reviews of a specific cosmetic
    @Operation(
            summary = "Get all reviews of a cosmetic",
            description = "특정 화장품의 리뷰를 모두 가져옵니다.",
            tags = {"Review Operations"},
            parameters = { @Parameter(name="cosmeticId", description = "화장품 ID")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = Review.class, type = "array"))),
                    @ApiResponse(responseCode = "404", description = "리뷰 없음", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/{cosmeticId}")
    public ResponseEntity<List<Review>> getReviewsForCosmetic(@PathVariable String cosmeticId) {
        Cosmetic cosmetic = cosmeticService.getCosmeticById(cosmeticId);
        if (cosmetic == null) {
            return ResponseEntity.notFound().build();
        }
        List<Review> reviews = reviewService.getAllReviewsByCosmetic(cosmetic);
        return ResponseEntity.ok(reviews);
    }

    @Operation(
            summary = "Add a new review",
            description = "새 리뷰를 추가합니다.",
            requestBody = @RequestBody(description = "Review details and images"),
            tags = {"Review Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "리뷰가 생성됨", content = @Content(schema = @Schema(implementation = Review.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/new")
    public Review addReview(
            @RequestPart("review") ReviewDTO reviewDTO,
            @RequestPart("images") MultipartFile[] images) {

        Review review = Review.builder()
                .content(reviewDTO.getContent())
                .rating(reviewDTO.getRating())
                .build();
        return reviewService.addReview(review, images);
    }

    @Operation(
            summary = "Update an existing review",
            description = "기존 리뷰를 업데이트합니다.",
            requestBody = @RequestBody(description = "Review update details and images"),
            tags = {"Review Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "리뷰가 업데이트됨", content = @Content(schema = @Schema(implementation = Review.class))),
                    @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PutMapping("/update/{id}")
    public ResponseEntity<Review> updateReview(
            @PathVariable("id") String id,
            @RequestPart Review reviewDetails,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        try {
            Review updatedReview = reviewService.updateReview(id, reviewDetails, images);
            return ResponseEntity.ok(updatedReview);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(
            summary = "Load an image",
            description = "이미지를 로드합니다.",
            tags = {"Image Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "이미지 로드 성공", content = @Content(schema = @Schema(implementation = Resource.class))),
                    @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("/image")
    public ResponseEntity<Resource> loadImage(@RequestParam("filename") String filename) {
        Resource file = fileStorageService.loadFile(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }

}
