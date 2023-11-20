package app.beautyminder.controller.search;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.MongoService;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import app.beautyminder.service.cosmetic.CosmeticSearchService;
import app.beautyminder.service.cosmetic.ReviewSearchService;
import app.beautyminder.util.AuthenticatedUser;
import com.mongodb.BasicDBObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static app.beautyminder.domain.User.MAX_HISTORY_SIZE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/search") // /search/

public class SearchController {

    private final CosmeticSearchService cosmeticSearchService;
    private final CosmeticRankService cosmeticRankService;
    private final ReviewSearchService reviewSearchService;
    private final MongoTemplate mongoTemplate;

    @Operation(summary = "Search Cosmetics by Name", description = "이름으로 화장품을 검색합니다. [USER 권한 필요]", tags = {"Search Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cosmetic.class)))), @ApiResponse(responseCode = "400", description = "Invalid parameters")})
    @GetMapping("/cosmetic")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<Cosmetic>> searchByName(@RequestParam String name, @Parameter(hidden = true) @AuthenticatedUser User user) {
        String trimmedName = (name != null) ? name.trim() : "";
        List<Cosmetic> results = cosmeticSearchService.searchByName(trimmedName);

        if (!results.isEmpty()) {
            updateUserSearchHistory(user, trimmedName);

            cosmeticRankService.collectSearchEvent(trimmedName);
            results.forEach(cosmetic -> cosmeticRankService.collectHitEvent(cosmetic.getId()));
        }
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Search Reviews by Content", description = "콘텐츠로 리뷰를 검색합니다. [USER 권한 필요]", tags = {"Search Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Review.class)))), @ApiResponse(responseCode = "400", description = "Invalid parameters")})
    @GetMapping("/review")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> searchByContent(@RequestParam String content, @Parameter(hidden = true) @AuthenticatedUser User user) {
        String trimmedName = (content != null) ? content.trim() : "";
        if (trimmedName.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not allowed empty keyword.");
        }

        List<Review> results = reviewSearchService.searchByContent(trimmedName);
        if (!results.isEmpty()) {
            updateUserSearchHistory(user, trimmedName);

            cosmeticRankService.collectSearchEvent(trimmedName);
            results.forEach(review -> cosmeticRankService.collectHitEvent(review.getCosmetic().getId()));
        }
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Search Cosmetics by Category", description = "카테고리로 화장품을 검색합니다. [USER 권한 필요]", tags = {"Search Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cosmetic.class)))), @ApiResponse(responseCode = "400", description = "Invalid parameters")})
    @GetMapping("/category")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<Cosmetic>> searchByCategory(@RequestParam String category, @Parameter(hidden = true) @AuthenticatedUser User user) {
        String trimmedName = (category != null) ? category.trim() : "";
        List<Cosmetic> results = cosmeticSearchService.searchByCategory(trimmedName);
        if (!results.isEmpty()) {
            updateUserSearchHistory(user, trimmedName);

            cosmeticRankService.collectSearchEvent(trimmedName);
            results.forEach(cosmetic -> cosmeticRankService.collectHitEvent(cosmetic.getId()));
        }
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Search Cosmetics by Keyword", description = "키워드로 화장품을 검색합니다. [USER 권한 필요]", tags = {"Search Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cosmetic.class)))), @ApiResponse(responseCode = "400", description = "Invalid parameters")})
    @GetMapping("/keyword")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<Cosmetic>> searchByKeyword(@RequestParam String keyword, @Parameter(hidden = true) @AuthenticatedUser User user) {
        String trimmedName = (keyword != null) ? keyword.trim() : "";
        List<Cosmetic> results = cosmeticSearchService.searchByKeyword(trimmedName);
        if (!results.isEmpty()) {
            updateUserSearchHistory(user, trimmedName);

            cosmeticRankService.collectSearchEvent(trimmedName);
            results.forEach(cosmetic -> cosmeticRankService.collectHitEvent(cosmetic.getId()));
        }
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Search Cosmetics by anything", description = "모든 데이터(화장품 이름,카테고리,키워드 + 리뷰 텍스트)를 검색합니다. [USER 권한 필요]", tags = {"Search Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cosmetic.class, type = "array")))), @ApiResponse(responseCode = "400", description = "Invalid parameters")})
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> searchAnything(@RequestParam String anything, @Parameter(hidden = true) @AuthenticatedUser User user) {
        String trimmedName = (anything != null) ? anything.trim() : "";
        if (trimmedName.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not allowed empty keyword.");
        }

        List<Cosmetic> nameResult = cosmeticSearchService.searchByName(trimmedName);
        List<Cosmetic> keywordResult = cosmeticSearchService.searchByKeyword(trimmedName);
        List<Cosmetic> cateResult = cosmeticSearchService.searchByCategory(trimmedName);
        List<Cosmetic> reviewResult = reviewSearchService.searchByContent(trimmedName).stream().map(Review::getCosmetic).toList();

        // Use LinkedHashSet to maintain order and avoid duplicates
        Set<Cosmetic> uniqueResults = new LinkedHashSet<>();

        // Method to get top 3 results from a list
        Function<List<Cosmetic>, List<Cosmetic>> topThree = list -> list.stream()
                .limit(3)
                .filter(uniqueResults::add) // Add only if not already present
                .collect(Collectors.toList());

        // Add top 3 results from each list to the set
        topThree.apply(nameResult);
        topThree.apply(keywordResult);
        topThree.apply(cateResult);
        topThree.apply(reviewResult);

        // Method to append remaining results to the set
        Consumer<List<Cosmetic>> appendRemaining = list -> {
            if (list.size() > 3) {
                uniqueResults.addAll(list.subList(3, list.size()));
            }
        };

        // Append remaining results from each list to the set
        appendRemaining.accept(nameResult);
        appendRemaining.accept(keywordResult);
        appendRemaining.accept(cateResult);
        appendRemaining.accept(reviewResult);

        var finalResult = new ArrayList<>(uniqueResults);

        if (!finalResult.isEmpty()) {
            updateUserSearchHistory(user, trimmedName);

            cosmeticRankService.collectSearchEvent(trimmedName);
            finalResult.forEach(cosmetic -> cosmeticRankService.collectHitEvent(cosmetic.getId()));
        }
        return ResponseEntity.ok(finalResult);
    }

    @Operation(summary = "Trigger ranking save", description = "랭킹을 강제로 저장합니다. [ADMIN 권한 필요]", tags = {"Search Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cosmetic.class, type = "array")))), @ApiResponse(responseCode = "400", description = "Invalid parameters")})
    @GetMapping("/test")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> searchRank(@RequestParam String anything) {
        cosmeticRankService.collectSearchEvent(anything.trim());
        return ResponseEntity.ok("ok");
    }

    @Async
    public void updateUserSearchHistory(User user, String searchQuery) {
        if (!searchQuery.isEmpty()) {
            return;
        }

        LinkedList<String> keywordHistory = new LinkedList<>(user.getKeywordHistory());

        // Remove the search query if it already exists to avoid duplication
        keywordHistory.remove(searchQuery);

        // Add the search query at the beginning of the list
        keywordHistory.addFirst(searchQuery);

        // Ensure the list doesn't exceed the maximum size
        while (keywordHistory.size() > User.MAX_HISTORY_SIZE) {
            keywordHistory.removeLast();
        }

        // Update the user document
        Query query = new Query(Criteria.where("id").is(user.getId()));
        Update update = new Update().set("keywordHistory", keywordHistory);
        mongoTemplate.findAndModify(query, update, User.class);
    }
}
