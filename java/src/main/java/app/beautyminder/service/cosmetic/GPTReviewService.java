package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.GPTReview;
import app.beautyminder.domain.Review;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.GPTReviewRepository;
import app.beautyminder.repository.ReviewRepository;
import io.github.flashvayne.chatgpt.dto.chat.MultiChatMessage;
import io.github.flashvayne.chatgpt.service.ChatgptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class GPTReviewService {

    private final ChatgptService chatgptService;
    private final CosmeticRepository cosmeticRepository;
    private final ReviewRepository reviewRepository;
    private final GPTReviewRepository gptReviewRepository;
    private static final Logger logger = LoggerFactory.getLogger(GPTReviewService.class);

    @Value("${chatgpt.system}")
    private String systemRole;

    @Value("${chatgpt.multi.model}")
    private String gptVersion;

    @Scheduled(cron = "0 0 7 ? * MON") // Every Monday at 7:00 am
    public void summarizeReviews() {
//        System.out.println("====== " + systemRole);
        List<Cosmetic> allCosmetics = cosmeticRepository.findAll();

        for (Cosmetic cosmetic : allCosmetics) {
            List<Review> positiveReviews = reviewRepository.findRandomReviewsByRatingAndCosmetic(3, 5, cosmetic.getId(), 10);
            List<Review> negativeReviews = reviewRepository.findRandomReviewsByRatingAndCosmetic(1, 3, cosmetic.getId(), 10);

            String positiveSummary = saveSummarizedReviews(positiveReviews, cosmetic);
            String negativeSummary = saveSummarizedReviews(negativeReviews, cosmetic);

            // Check if GPTReview already exists for this cosmetic
            Optional<GPTReview> existingReviewOpt = gptReviewRepository.findByCosmetic(cosmetic);

            if (existingReviewOpt.isPresent()) {
                // If it exists, update it
                GPTReview existingReview = existingReviewOpt.get();
                existingReview.setPositive(positiveSummary);
                existingReview.setNegative(negativeSummary);
                gptReviewRepository.save(existingReview);
            } else {
                // If it doesn't exist, create a new one
                GPTReview gptReview = GPTReview.builder()
                        .gptVersion(gptVersion)
                        .positive(positiveSummary)
                        .negative(negativeSummary)
                        .cosmetic(cosmetic)
                        .build();
                gptReviewRepository.save(gptReview);
            }
        }

        log.info("GPTReview: Summarization done");
    }

    private String saveSummarizedReviews(List<Review> reviews, Cosmetic cosmetic) {
        logger.info("Summarizing reviews for {}...", cosmetic.getName());
        StringBuilder allContents = new StringBuilder();
        allContents.append("제품명: ").append(cosmetic.getName()).append("\n");

        int index = 1;
        for (Review review : reviews) {
            allContents.append("리뷰").append(index).append(". ").append(review.getContent()).append("\n");
            index++;
        }

        List<MultiChatMessage> messages = Arrays.asList(
                new MultiChatMessage("system", systemRole),
                new MultiChatMessage("user", allContents.toString()));

        return chatgptService.multiChat(messages); // Return the summarized content
    }
}