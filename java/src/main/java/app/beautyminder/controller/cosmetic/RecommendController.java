package app.beautyminder.controller.cosmetic;

import app.beautyminder.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recommend")
public class RecommendController {
    private final RecommendService recommendService;


    @GetMapping("/{userId}")
    public ResponseEntity<?> getRecommendation(@PathVariable String userId) {
        return ResponseEntity.ok(recommendService.recommendProducts(userId));
    }
}
