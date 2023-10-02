package app.beautyminder.controller;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.service.cosmetic.CosmeticService;
import app.beautyminder.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cosmetic") // /review/api
public class CosmeticApiController {

    private final ReviewService reviewService;
    private final CosmeticService cosmeticService;

    @GetMapping
    public ResponseEntity<List<Cosmetic>> getAllCosmetics() {
        List<Cosmetic> cosmetics = cosmeticService.getAllCosmetics();
        return ResponseEntity.ok(cosmetics);
    }

    // Retrieve a specific cosmetic by its ID
    @GetMapping("/{id}")
    public ResponseEntity<Cosmetic> getCosmeticById(@PathVariable String id) {
        Cosmetic cosmetic = cosmeticService.getCosmeticById(id);
        if (cosmetic == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cosmetic);
    }

    // Add a new cosmetic
    @PostMapping
    public ResponseEntity<Cosmetic> createCosmetic(@RequestBody Cosmetic cosmetic) {
        Cosmetic newCosmetic = cosmeticService.saveCosmetic(cosmetic);
        return ResponseEntity.ok(newCosmetic);
    }

    // Update an existing cosmetic
    @PutMapping("/{id}")
    public ResponseEntity<Cosmetic> updateCosmetic(@PathVariable String id, @RequestBody Cosmetic cosmeticDetails) {
        Cosmetic updatedCosmetic = cosmeticService.updateCosmetic(id, cosmeticDetails);
        if (updatedCosmetic == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedCosmetic);
    }

    // Delete a cosmetic
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCosmetic(@PathVariable String id) {
        boolean deleted = cosmeticService.deleteCosmetic(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    // Retrieve all reviews of a specific cosmetic
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<Review>> getReviewsForCosmetic(@PathVariable String id) {
        Cosmetic cosmetic = cosmeticService.getCosmeticById(id);
        if (cosmetic == null) {
            return ResponseEntity.notFound().build();
        }
        List<Review> reviews = reviewService.getAllReviewsByCosmetic(cosmetic);
        return ResponseEntity.ok(reviews);
    }
}
