package app.beautyminder.controller.review;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.GPTReview;
import app.beautyminder.repository.GPTReviewRepository;
import app.beautyminder.service.cosmetic.CosmeticService;
import app.beautyminder.service.cosmetic.GPTService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/gpt")
public class GPTReviewController {

    private final GPTService gptService;
    private final CosmeticService cosmeticService;
    private final GPTReviewRepository gptReviewRepository;

    @Operation(
            summary = "Trigger Summarization",
            description = "리뷰 요약을 트리거합니다. [ADMIN 권한 필요]",
            tags = {"GPT Review Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "리뷰 요약 성공적으로 완료",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청",
                            content = @Content(schema = @Schema(implementation = String.class)))
            }
    )

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/review/summarize")
    public ResponseEntity<String> triggerSummarization() { // test call
        gptService.summarizeReviews();
        return ResponseEntity.ok("Reviews summarized successfully!");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/review/summarize/{cosmeticId}")
    public ResponseEntity<String> triggerOneSummarization(@PathVariable String cosmeticId) { // test call
        gptService.summaryCosmetic(cosmeticId);
        return ResponseEntity.ok("Reviews summarized for " + cosmeticId + " by GPT-4 successfully!");
    }

    @Operation(
            summary = "Get Cosmetic's a GPT-review by ID",
            description = "화장품 ID로 GPT 요약 리뷰를 가져옵니다.",
            tags = {"GPT Review Operations"},
            parameters = {
                    @Parameter(name = "id", description = "화장품의 ID")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "GPT 리뷰 정보 검색 성공", content = @Content(schema = @Schema(implementation = GPTReview.class))),
                    @ApiResponse(responseCode = "404", description = "GPT 리뷰 정보를 찾을 수 없음", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/review/{id}")
    public ResponseEntity<?> getGPTReview(@PathVariable String id) {
        Cosmetic cosmetic = cosmeticService.getCosmeticById(id);
        if (cosmetic == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<GPTReview> review = gptReviewRepository.findByCosmetic(cosmetic);

        return review.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
