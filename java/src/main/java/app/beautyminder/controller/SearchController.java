package app.beautyminder.controller;

import app.beautyminder.domain.*;
import app.beautyminder.service.ReviewService;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticMetricService;
import app.beautyminder.service.cosmetic.CosmeticSearchService;
import app.beautyminder.service.cosmetic.CosmeticService;
import app.beautyminder.service.cosmetic.ReviewSearchService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/search") // /search/
public class SearchController {

    private final CosmeticSearchService cosmeticSearchService;
    private final ReviewSearchService reviewSearchService;
    private final CosmeticMetricService cosmeticMetricService;

    @GetMapping("/cosmetic")
    public ResponseEntity<List<EsCosmetic>> searchByName(@RequestParam String name) {
        List<EsCosmetic> results = cosmeticSearchService.searchByName(name);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/review")
    public ResponseEntity<List<EsReview>> searchByContent(@RequestParam String content) {
        List<EsReview> results = reviewSearchService.searchByContent(content);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/category")
    public ResponseEntity<List<EsCosmetic>> searchByCategory(@RequestParam String category) {
        List<EsCosmetic> results = cosmeticSearchService.searchByCategory(category);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/keyword")
    public ResponseEntity<List<EsCosmetic>> searchByKeyword(@RequestParam String keyword) {
        List<EsCosmetic> results = cosmeticSearchService.searchByKeyword(keyword);
        return ResponseEntity.ok(results);
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

    @PostMapping("/index/review")
    public ResponseEntity<String> triggerIndexingReview() { // force indexing reviews
        reviewSearchService.indexReviews();
        return ResponseEntity.ok("index successfully!");
    }
}
