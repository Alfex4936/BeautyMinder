package app.beautyminder.controller.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.User;
import app.beautyminder.service.RecommendService;
import app.beautyminder.util.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recommend")
@PreAuthorize("hasRole('ROLE_USER')")
public class RecommendController {
    private final RecommendService recommendService;

    @Operation(
            summary = "Get product recommendation.",
            description = "추천 제품을 로딩합니다. [User 권한 필요]",
            tags = {"Recommend Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cosmetic 리스트 불러오기 완료",
                            content = @Content(schema = @Schema(implementation = Cosmetic.class, type = "array"))),
            }
    )
    @GetMapping()
    public ResponseEntity<?> getRecommendation(@Parameter(hidden = true) @AuthenticatedUser User user) {
        return ResponseEntity.ok(recommendService.recommendProducts(user.getId()));
    }
}
