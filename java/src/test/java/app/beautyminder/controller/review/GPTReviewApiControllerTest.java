package app.beautyminder.controller.review;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.GPTReview;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.repository.GPTReviewRepository;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticService;
import app.beautyminder.service.cosmetic.GPTService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
public class GPTReviewApiControllerTest {

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(3);

    private static final String TEST_ADMIN_EMAIL = "admintest@gmail.com";
    private static final String TEST_ADMIN_PASSWORD = "test";

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    private GPTService gptService;
    @MockBean
    private CosmeticService cosmeticService;
    @MockBean
    private GPTReviewRepository gptReviewRepository;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenProvider tokenProvider;

    private String accessToken;
    private String userId;

    private Cosmetic createTestCosmetic() {
        LocalDate expirationDate = LocalDate.now().plusYears(1);
        LocalDate purchasedDate = LocalDate.now().minusDays(30);
        return Cosmetic.builder().id("test").name("Test Cosmetic").brand("Test Brand").expirationDate(expirationDate).purchasedDate(purchasedDate).category("Test Category").reviewCount(0).totalRating(0).favCount(0).build();
    }

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @BeforeAll
    public void initialize() {
        AddUserRequest addUserRequest = new AddUserRequest();
        addUserRequest.setEmail(TEST_ADMIN_EMAIL);
        addUserRequest.setPassword(TEST_ADMIN_PASSWORD);

        User user = userService.saveAdmin(addUserRequest);
        userId = user.getId();
        accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
    }


    @Test
    public void testCallReviewSummary() throws Exception {
        doNothing().when(gptService).summarizeReviews();

        mockMvc.perform(get("/gpt/review/summarize")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testCallOneSummary() throws Exception {
        doNothing().when(gptService).summaryCosmetic(anyString());

        mockMvc.perform(get("/gpt/review/summarize/" + "testId")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetGPTReview_Success() throws Exception {
        var cosmetic = createTestCosmetic();
        var gptReview = GPTReview.builder().build();
        when(cosmeticService.getCosmeticById(anyString())).thenReturn(cosmetic);
        when(gptReviewRepository.findByCosmetic(any(Cosmetic.class))).thenReturn(Optional.ofNullable(gptReview));

        mockMvc.perform(get("/gpt/review/" + cosmetic.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetGPTReview_FailByNullCosmetic() throws Exception {
        var cosmetic = createTestCosmetic();
        when(cosmeticService.getCosmeticById(anyString())).thenReturn(null);

        mockMvc.perform(get("/gpt/review/" + cosmetic.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetGPTReview_FailByNullGPTReview() throws Exception {
        var cosmetic = createTestCosmetic();
        when(cosmeticService.getCosmeticById(anyString())).thenReturn(null);
        when(gptReviewRepository.findByCosmetic(any(Cosmetic.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/gpt/review/" + cosmetic.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @AfterEach
    public void cleanUp() {
        // Clean up logic to run after each test if needed
    }

    @AfterAll
    public void cleanUpAll() {
        try {
            // Final cleanup logic to run after all tests
            userService.deleteUserAndRelatedData(userId);
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }
    }
}
