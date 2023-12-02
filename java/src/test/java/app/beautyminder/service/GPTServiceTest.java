package app.beautyminder.service;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.GPTReview;
import app.beautyminder.domain.Review;
import app.beautyminder.dto.chat.ChatMessage;
import app.beautyminder.dto.chat.ChatRoom;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.GPTReviewRepository;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.service.chat.ChatService;
import app.beautyminder.service.cosmetic.CosmeticService;
import app.beautyminder.service.cosmetic.GPTService;
import app.beautyminder.service.review.ReviewService;
import io.github.flashvayne.chatgpt.dto.chat.MultiChatRequest;
import io.github.flashvayne.chatgpt.service.ChatgptService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GPTServiceTest {

    private ChatgptService chatgptService;
    private CosmeticRepository cosmeticRepository;
    private ReviewRepository reviewRepository;
    private ReviewService reviewService;
    private GPTReviewRepository gptReviewRepository;

    private GPTService gptService;

    private List<Cosmetic> testCosmetics;
    private List<Review> testPositiveReviews;
    private List<Review> testNegativeReviews;

    private Cosmetic createTestCosmetic() {
        LocalDate expirationDate = LocalDate.now().plusYears(1);
        LocalDate purchasedDate = LocalDate.now().minusDays(30);
        return Cosmetic.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Cosmetic")
                .brand("Test Brand")
                .expirationDate(expirationDate)
                .purchasedDate(purchasedDate)
                .category("Test Category")
                .reviewCount(0)
                .totalRating(0)
                .favCount(0)
                .build();
    }

    private Review createTestReview() {
        return Review.builder()
                .content("YAY")
                .rating(3)
                .build();
    }

    @BeforeEach
    void setUp() {
        chatgptService = mock(ChatgptService.class);
        cosmeticRepository = mock(CosmeticRepository.class);
        reviewService = mock(ReviewService.class);
        gptReviewRepository = mock(GPTReviewRepository.class);
        gptService = new GPTService(chatgptService, cosmeticRepository, reviewService, gptReviewRepository);

        // Create a list of test cosmetics
        testCosmetics = IntStream.range(0, 5) // Assuming 5 test cosmetics
                .mapToObj(i -> createTestCosmetic())
                .collect(Collectors.toList());

        // Create lists of test reviews
        testPositiveReviews = IntStream.range(0, 10) // Assuming 10 positive reviews
                .mapToObj(i -> createTestReview())
                .collect(Collectors.toList());

        testNegativeReviews = IntStream.range(0, 10) // Assuming 10 negative reviews
                .mapToObj(i -> createTestReview())
                .collect(Collectors.toList());
    }

    @Test
    public void summarizeReviews_ShouldSummarizeAllReviews() {
        when(cosmeticRepository.findAll()).thenReturn(testCosmetics);
        for (Cosmetic cosmetic : testCosmetics) {
            when(reviewService.findRandomReviewsByRatingAndCosmetic(anyInt(), anyInt(), eq(cosmetic.getId()), anyInt()))
                    .thenReturn(testPositiveReviews, testNegativeReviews);
        }
        when(chatgptService.multiChat(anyList())).thenReturn("Mocked Summary");

        gptService.summarizeReviews();

        verify(cosmeticRepository, times(1)).findAll();
        for (Cosmetic cosmetic : testCosmetics) {
            verify(reviewService, times(2)).findRandomReviewsByRatingAndCosmetic(anyInt(), anyInt(), eq(cosmetic.getId()), anyInt());
        }
        verify(gptReviewRepository, atLeastOnce()).save(any(GPTReview.class));
    }

    @Test
    public void summaryCosmetic_ShouldSummarizeForGivenCosmetic() {
        String testCosmeticId = "testCosmeticId";
        Cosmetic testCosmetic = createTestCosmetic();

        when(cosmeticRepository.findById(testCosmeticId)).thenReturn(Optional.of(testCosmetic));
        when(reviewService.findRandomReviewsByRatingAndCosmetic(anyInt(), anyInt(), eq(testCosmetic.getId()), anyInt()))
                .thenReturn(testPositiveReviews)
                .thenReturn(testNegativeReviews);
        when(chatgptService.multiChat(anyList())).thenReturn("Mocked Summary");

        gptService.summaryCosmetic(testCosmeticId);

        verify(cosmeticRepository, times(1)).findById(testCosmeticId);
        verify(reviewService, times(2)).findRandomReviewsByRatingAndCosmetic(anyInt(), anyInt(), eq(testCosmetic.getId()), anyInt());
        verify(gptReviewRepository, atLeastOnce()).save(any(GPTReview.class));
    }


    @Test
    public void generateNotice_ShouldReturnGeneratedNotice() {
        String baumannType = "OSNT";
        when(chatgptService.multiChat(anyList(), any(MultiChatRequest.class))).thenReturn("Generated Notice");

        String result = gptService.generateNotice(baumannType);

        assertNotNull(result);
        assertEquals("Generated Notice", result);
        verify(chatgptService, times(1)).multiChat(anyList(), any(MultiChatRequest.class));
    }
}