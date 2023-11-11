package app.beautyminder.controller.elasticsearch;

import app.beautyminder.service.LogService;
import app.beautyminder.service.cosmetic.CosmeticSearchService;
import app.beautyminder.service.cosmetic.ReviewSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

// TODO: change all to ADMIN_ROLE
@ControllerAdvice
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/es-index")
public class EsIndexController {

    private final CosmeticSearchService cosmeticSearchService;
    private final ReviewSearchService reviewSearchService;
    private final LogService logService;

    @GetMapping("/cosmetics/list")
    public ResponseEntity<String> listAllIndices() throws IOException {
        String indices = cosmeticSearchService.listAllIndices();
        return ResponseEntity.ok(indices);
    }

    @GetMapping("/cosmetics")
    public ResponseEntity<String> getIndexOfCosmetics() throws IOException {
        String indexInfo = cosmeticSearchService.getIndexOfCosmetics();
        return ResponseEntity.ok(indexInfo);
    }

    @DeleteMapping("/cosmetics/delete")
    public ResponseEntity<Void> deleteCosmeticDocuments() {
        logService.deleteAllDocuments("cosmetics");
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reviews/delete")
    public ResponseEntity<String> dropReviewDocuments() {
        logService.deleteAllDocuments("reviews");
        return ResponseEntity.ok("Deleted review indices successfully");
    }

    @PostMapping("/reviews/index")
    public ResponseEntity<String> triggerIndexingReview() {
        reviewSearchService.indexReviews();
        return ResponseEntity.ok("Indexed review successfully!");
    }
}
