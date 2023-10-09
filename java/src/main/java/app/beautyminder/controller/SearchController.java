package app.beautyminder.controller;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.EsCosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.service.ReviewService;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticSearchService;
import app.beautyminder.service.cosmetic.CosmeticService;
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

    @GetMapping("/name")
    public ResponseEntity<List<EsCosmetic>> searchByName(@RequestParam String name) {
        List<EsCosmetic> results = cosmeticSearchService.searchByName(name);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/category")
    public ResponseEntity<List<EsCosmetic>> searchByCategory(@RequestParam Cosmetic.Category category) {
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
}
