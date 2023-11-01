package app.beautyminder.controller.redis;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.repository.KeywordRankRepository;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/redis")
public class RedisController {

    private final CosmeticRankService cosmeticRankService;
    private final KeywordRankRepository keywordRankRepository;

    @Operation(
            summary = "Get N ranked products",
            description = "N개의 실시간 랭킹 제품들을 불러옵니다.",
            tags = {"Redis Operations"},
            parameters = {
                    @Parameter(name = "size", description = "N개 제품")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = Cosmetic.class, type = "array")))
            }
    )
    @GetMapping("/top/cosmetics")
    public ResponseEntity<List<Cosmetic>> getTopRankedCosmetics(
            @RequestParam(defaultValue = "10") int size) {
        List<Cosmetic> topRankedCosmetics = cosmeticRankService.getTopRankedCosmetics(size);
        return ResponseEntity.ok(topRankedCosmetics);
    }

    @Operation(
            summary = "Get top 10 keywords",
            description = "가장 최근의 검색어 랭킹을 불러옵니다.",
            tags = {"Redis Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = String.class, type = "array")))
            }
    )
    @GetMapping("/top/keywords")
    public ResponseEntity<List<String>> getTopRankedKeywords() {
        return keywordRankRepository.findTopByOrderByCreatedAtDesc()
                .map(rank -> ResponseEntity.ok(rank.getRankings()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList()));
    }
}
