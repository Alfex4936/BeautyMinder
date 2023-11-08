package app.beautyminder.controller.search;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import app.beautyminder.service.cosmetic.CosmeticSearchService;
import app.beautyminder.service.cosmetic.ReviewSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/search") // /search/
public class SearchController {

    private final CosmeticSearchService cosmeticSearchService;
    private final CosmeticRankService cosmeticRankService;
    private final ReviewSearchService reviewSearchService;

    @Operation(summary = "Search Cosmetics by Name", description = "이름으로 화장품을 검색합니다.", tags = {"Search Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cosmetic.class)))), @ApiResponse(responseCode = "400", description = "Invalid parameters")})
    @GetMapping("/cosmetic")
    public ResponseEntity<List<Cosmetic>> searchByName(@RequestParam String name) {
        List<Cosmetic> results = cosmeticSearchService.searchByName(name);
        if (!results.isEmpty()) {
            cosmeticRankService.collectSearchEvent(name.trim());
        }
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Search Reviews by Content", description = "콘텐츠로 리뷰를 검색합니다.", tags = {"Search Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Review.class)))), @ApiResponse(responseCode = "400", description = "Invalid parameters")})
    @GetMapping("/review")
    public ResponseEntity<List<Review>> searchByContent(@RequestParam String content) {
        List<Review> results = reviewSearchService.searchByContent(content);
        if (!results.isEmpty()) {
            cosmeticRankService.collectSearchEvent(content.trim());
        }
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Search Cosmetics by Category", description = "카테고리로 화장품을 검색합니다.", tags = {"Search Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cosmetic.class)))), @ApiResponse(responseCode = "400", description = "Invalid parameters")})
    @GetMapping("/category")
    public ResponseEntity<List<Cosmetic>> searchByCategory(@RequestParam String category) {
        List<Cosmetic> results = cosmeticSearchService.searchByCategory(category);
        if (!results.isEmpty()) {
            cosmeticRankService.collectSearchEvent(category.trim());
        }
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Search Cosmetics by Keyword", description = "키워드로 화장품을 검색합니다.", tags = {"Search Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cosmetic.class)))), @ApiResponse(responseCode = "400", description = "Invalid parameters")})
    @GetMapping("/keyword")
    public ResponseEntity<List<Cosmetic>> searchByKeyword(@RequestParam String keyword) {
        List<Cosmetic> results = cosmeticSearchService.searchByKeyword(keyword);
        if (!results.isEmpty()) {
            cosmeticRankService.collectSearchEvent(keyword.trim());
        }
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Search Cosmetics by anything", description = "모든 데이터(화장품 이름,카테고리,키워드 + 리뷰 텍스트)를 검색합니다.", tags = {"Search Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cosmetic.class, type = "array")))), @ApiResponse(responseCode = "400", description = "Invalid parameters")})
    @GetMapping
    public ResponseEntity<?> searchAnything(@RequestParam String anything) {

        List<Cosmetic> keywordResult = cosmeticSearchService.searchByKeyword(anything);
        List<Cosmetic> cateResult = cosmeticSearchService.searchByCategory(anything);
        List<Cosmetic> nameResult = cosmeticSearchService.searchByName(anything);
        List<Cosmetic> reviewResult = reviewSearchService.searchByContent(anything).stream().map(Review::getCosmetic).toList();

        Set<Cosmetic> finalResult = new HashSet<>();
        finalResult.addAll(keywordResult);
        finalResult.addAll(cateResult);
        finalResult.addAll(nameResult);
        finalResult.addAll(reviewResult);
        if (!finalResult.isEmpty()) {
            cosmeticRankService.collectSearchEvent(anything.trim());
        }
//        return ResponseEntity.ok("ok");
        return ResponseEntity.ok(finalResult);
    }
}
