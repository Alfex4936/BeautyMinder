package app.beautyminder.controller;

import app.beautyminder.domain.EsCosmetic;
import app.beautyminder.domain.EsReview;
import app.beautyminder.dto.CosmeticMetricData;
import app.beautyminder.service.cosmetic.CosmeticMetricService;
import app.beautyminder.service.cosmetic.CosmeticSearchService;
import app.beautyminder.service.cosmetic.ReviewSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/search") // /search/
public class SearchController {

    private final CosmeticSearchService cosmeticSearchService;
    private final CosmeticMetricService cosmeticMetricService;
    private final ReviewSearchService reviewSearchService;

    @Operation(
            summary = "Search Cosmetics by Name",
            description = "이름으로 화장품을 검색합니다.",
            tags = {"Search Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation"),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters")
            }
    )
    @GetMapping("/cosmetic")
    public ResponseEntity<List<EsCosmetic>> searchByName(@RequestParam String name) {
        List<EsCosmetic> results = cosmeticSearchService.searchByName(name);
        if (!results.isEmpty()) {
            cosmeticMetricService.collectSearchEvent(name.trim());
        }
        return ResponseEntity.ok(results);
    }

    @Operation(
            summary = "Search Reviews by Content",
            description = "콘텐츠로 리뷰를 검색합니다.",
            tags = {"Search Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation", content =
                    @Content(array = @ArraySchema(
                            schema = @Schema(implementation = EsReview.class)
                    ))),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters")
            }
    )
    @GetMapping("/review")
    public ResponseEntity<List<EsReview>> searchByContent(@RequestParam String content) {
        List<EsReview> results = reviewSearchService.searchByContent(content);
        if (!results.isEmpty()) {
            cosmeticMetricService.collectSearchEvent(content.trim());
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/review/drop")

    public ResponseEntity<String> dropReviewIndices() {
        reviewSearchService.deleteAllIndices();
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Search Cosmetics by Category",
            description = "카테고리로 화장품을 검색합니다.",
            tags = {"Search Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation", content =
                    @Content(array = @ArraySchema(
                            schema = @Schema(implementation = EsCosmetic.class)
                    ))),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters")
            }
    )
    @GetMapping("/category")
    public ResponseEntity<List<EsCosmetic>> searchByCategory(@RequestParam String category) {
        List<EsCosmetic> results = cosmeticSearchService.searchByCategory(category);
        if (!results.isEmpty()) {
            cosmeticMetricService.collectSearchEvent(category.trim());
        }
        return ResponseEntity.ok(results);
    }

    @Operation(
            summary = "Search Cosmetics by Keyword",
            description = "키워드로 화장품을 검색합니다.",
            tags = {"Search Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation", content =
                    @Content(array = @ArraySchema(
                            schema = @Schema(implementation = EsCosmetic.class)
                    ))),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters")
            }
    )
    @GetMapping("/keyword")
    public ResponseEntity<List<EsCosmetic>> searchByKeyword(@RequestParam String keyword) {
        List<EsCosmetic> results = cosmeticSearchService.searchByKeyword(keyword);
        if (!results.isEmpty()) {
            cosmeticMetricService.collectSearchEvent(keyword.trim());
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/analyze")
    public ResponseEntity<String> analyzeReview(@RequestParam String text) throws IOException {
        String analyzedText = reviewSearchService.analyzeText(text);
        return ResponseEntity.ok(analyzedText);
    }

    @GetMapping("/indices")
    public ResponseEntity<String> listAllIndices() throws IOException {
        String indices = cosmeticSearchService.listAllIndices();
        return ResponseEntity.ok(indices);
    }

    @GetMapping("/indices/cosmetics")
    public ResponseEntity<String> getIndexOfCosmetics() throws IOException {
        String indexInfo = cosmeticSearchService.getIndexOfCosmetics();
        return ResponseEntity.ok(indexInfo);
    }

    @GetMapping("/cosmetics/data")
    public ResponseEntity<String> viewCosmeticsData() throws IOException {
        String cosmeticsData = cosmeticSearchService.viewCosmeticsData();
        return ResponseEntity.ok(cosmeticsData);
    }

    @GetMapping("/indices/metric")
    public ResponseEntity<String> viewCosmeticMetricsData() throws IOException {
        String cosmeticsData = cosmeticSearchService.viewCosmeticMetricsData();
        return ResponseEntity.ok(cosmeticsData);
    }

    @DeleteMapping("/indices")
    public ResponseEntity<Void> deleteAllIndices() {
        cosmeticSearchService.deleteAllIndices();
        return ResponseEntity.ok().build();  // Return a 200 OK response upon success
    }

//    @PostMapping("/index/cosmetic")
//    public ResponseEntity<String> triggerIndexingCosmetic() { // force indexing cosmetic metrics
//        cosmeticMetricService.executeBulkUpdates();
//        return ResponseEntity.ok("index successfully!");
//    }

    @Operation(
            summary = "Trigger Indexing of Reviews",
            description = "리뷰 인덱싱을 트리거합니다.",
            tags = {"Indexing Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Indexing successful", content = @Content(mediaType = "text/plain")),
                    @ApiResponse(responseCode = "500", description = "Indexing failed", content = @Content(mediaType = "text/plain"))
            }
    )
    @PostMapping("/index/review")
    public ResponseEntity<String> triggerIndexingReview() { // force indexing reviews
        reviewSearchService.indexReviews();
        return ResponseEntity.ok("index successfully!");
    }

    @GetMapping("/data")
    public ResponseEntity<List<CosmeticMetricData>> getCosmeticData() {
        List<CosmeticMetricData> data = cosmeticMetricService.getAllCosmeticCounts();
        return ResponseEntity.ok(data);
    }
}
