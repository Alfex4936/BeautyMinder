package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.GPTReview;
import app.beautyminder.domain.Review;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.GPTReviewRepository;
import app.beautyminder.service.review.ReviewService;
import io.github.flashvayne.chatgpt.dto.chat.ChatRole;
import io.github.flashvayne.chatgpt.dto.chat.MultiChatMessage;
import io.github.flashvayne.chatgpt.dto.chat.MultiChatRequest;
import io.github.flashvayne.chatgpt.service.ChatgptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class GPTService {

    private final ChatgptService chatgptService;
    private final CosmeticRepository cosmeticRepository;
    private final ReviewService reviewService;
    private final GPTReviewRepository gptReviewRepository;
    @Value("${chatgpt.system}")
    private String systemRole;

    @Value("${chatgpt.system-keyword}")
    private String systemRoleKeyword;

    @Value("${chatgpt.system-bot}")
    private String systemRoleBot;

    @Value("${chatgpt.multi.model}")
    private String gptVersion;

    // @Scheduled(cron = "0 0 7 ? * MON", zone = "Asia/Seoul") // Every Monday at 7:00 am
    public void summarizeReviews() {
//        System.out.println("====== " + systemRole);
        var allCosmetics = cosmeticRepository.findAll();

        allCosmetics.forEach(cosmetic -> {
            var positiveReviews = reviewService.findRandomReviewsByRatingAndCosmetic(3, 5, cosmetic.getId(), 10);
            var negativeReviews = reviewService.findRandomReviewsByRatingAndCosmetic(1, 3, cosmetic.getId(), 10);

            var positiveSummary = saveSummarizedReviews(positiveReviews, cosmetic);
            var negativeSummary = saveSummarizedReviews(negativeReviews, cosmetic);

            // Check if GPTReview already exists for this cosmetic
            gptReviewRepository.findByCosmetic(cosmetic).ifPresentOrElse(
                    existingReview -> {
                        existingReview.setPositive(positiveSummary);
                        existingReview.setNegative(negativeSummary);
                        gptReviewRepository.save(existingReview);
                    },
                    () -> gptReviewRepository.save(GPTReview.builder()
                            .gptVersion(gptVersion)
                            .positive(positiveSummary)
                            .negative(negativeSummary)
                            .cosmetic(cosmetic)
                            .build())
            );
        });

        log.info("BEMINDER: GPTReview: Summarization done");
    }

    public void summaryCosmetic(String cosmeticId) {
        var cosmetic = cosmeticRepository.findById(cosmeticId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "A cosmetic is not found."));

        var positiveReviews = reviewService.findRandomReviewsByRatingAndCosmetic(3, 5, cosmetic.getId(), 10);
        var negativeReviews = reviewService.findRandomReviewsByRatingAndCosmetic(1, 3, cosmetic.getId(), 10);

        var positiveSummary = saveSummarizedReviews(positiveReviews, cosmetic);
        var negativeSummary = saveSummarizedReviews(negativeReviews, cosmetic);

        // Check if GPTReview already exists for this cosmetic
        gptReviewRepository.findByCosmetic(cosmetic).ifPresentOrElse(
                existingReview -> {
                    existingReview.setPositive(positiveSummary);
                    existingReview.setNegative(negativeSummary);
                    gptReviewRepository.save(existingReview);
                },
                () -> gptReviewRepository.save(GPTReview.builder()
                        .gptVersion(gptVersion)
                        .positive(positiveSummary)
                        .negative(negativeSummary)
                        .cosmetic(cosmetic)
                        .build())
        );
    }

    private String saveSummarizedReviews(List<Review> reviews, Cosmetic cosmetic) {
        log.info("Summarizing reviews for {}...", cosmetic.getName());
        var allContents = new StringBuilder();
        allContents.append("제품명: ").append(cosmetic.getName()).append("\n");

        var reviewContents = reviews.stream()
                .map(review -> "리뷰" + reviews.indexOf(review) + 1 + ": " + review.getContent())
                .collect(Collectors.joining("\n"));

        allContents.append(reviewContents);

        List<MultiChatMessage> messages = Arrays.asList(
                new MultiChatMessage(ChatRole.SYSTEM, systemRole),
                new MultiChatMessage(ChatRole.USER, allContents.toString()));

        return chatgptService.multiChat(messages); // Return the summarized content
    }

    public String generateNotice(String baumannType) {
        var multiChatRequest = MultiChatRequest.builder()
                .maxTokens(128)
                .model("gpt-3.5-turbo-1106")
                .build();

        var messages = List.of(
                new MultiChatMessage(ChatRole.SYSTEM, systemRoleBot),
                new MultiChatMessage(ChatRole.USER, baumannType + "타입"));

        return chatgptService.multiChat(messages, multiChatRequest);
    }
}