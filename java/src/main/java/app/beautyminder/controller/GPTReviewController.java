package app.beautyminder.controller;

import app.beautyminder.service.cosmetic.GPTReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/gpt")
public class GPTReviewController {

    private final GPTReviewService gptReviewService;


    @PostMapping("/summarize")
    public ResponseEntity<String> triggerSummarization() { // test call
        gptReviewService.summarizeReviews();
        return ResponseEntity.ok("Reviews summarized successfully!");
    }
}
