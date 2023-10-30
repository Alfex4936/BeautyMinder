package app.beautyminder.controller.elasticsearch;

import app.beautyminder.service.cosmetic.CosmeticSearchService;
import app.beautyminder.service.cosmetic.ReviewSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@ControllerAdvice
@RequiredArgsConstructor
@RestController
@RequestMapping("/es-index")
public class EsIndexController {

    private final CosmeticSearchService cosmeticSearchService;
    private final ReviewSearchService reviewSearchService;

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
    public ResponseEntity<Void> deleteAllIndices() {
        cosmeticSearchService.deleteAllIndices();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reviews/delete")
    public ResponseEntity<String> dropReviewIndices() {
        reviewSearchService.deleteAllIndices();
        return ResponseEntity.ok("Deleted review indices successfully");
    }

    @PostMapping("/reviews/index")
    public ResponseEntity<String> triggerIndexingReview() {
        reviewSearchService.indexReviews();
        return ResponseEntity.ok("Indexed review successfully!");
    }
}
