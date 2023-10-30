package app.beautyminder.controller.elasticsearch;

import app.beautyminder.dto.CosmeticMetricData;
import app.beautyminder.service.cosmetic.CosmeticMetricService;
import app.beautyminder.service.cosmetic.CosmeticSearchService;
import app.beautyminder.service.cosmetic.ReviewSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/data-view")
public class DataViewController {
    private final CosmeticSearchService cosmeticSearchService;
    private final CosmeticMetricService cosmeticMetricService;
    private final ReviewSearchService reviewSearchService;

    @GetMapping("/review/analyze")
    public ResponseEntity<String> analyzeReview(@RequestParam String text) throws IOException {
        String analyzedText = reviewSearchService.analyzeText(text);
        return ResponseEntity.ok(analyzedText);
    }

    @GetMapping("/cosmetics")
    public ResponseEntity<String> viewCosmeticsData() throws IOException {
        String cosmeticsData = cosmeticSearchService.viewCosmeticsData();
        return ResponseEntity.ok(cosmeticsData);
    }
    
    @GetMapping("/cosmetic-metrics")
    public ResponseEntity<String> viewCosmeticMetricsData() throws IOException {
        String cosmeticsData = cosmeticSearchService.viewCosmeticMetricsData();
        return ResponseEntity.ok(cosmeticsData);
    }

    @GetMapping("/cosmetic-counts")
    public ResponseEntity<List<CosmeticMetricData>> getCosmeticData() {
        List<CosmeticMetricData> data = cosmeticMetricService.getAllCosmeticCounts();
        return ResponseEntity.ok(data);
    }
}
