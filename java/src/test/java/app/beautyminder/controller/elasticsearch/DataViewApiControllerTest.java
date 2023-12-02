package app.beautyminder.controller.elasticsearch;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.User;
import app.beautyminder.dto.CosmeticMetricData;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import app.beautyminder.service.cosmetic.CosmeticSearchService;
import app.beautyminder.service.cosmetic.ReviewSearchService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
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
public class DataViewApiControllerTest {

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(3);

    private static final String TEST_ADMIN_EMAIL = "dataviewadmin@test.com";
    private static final String TEST_ADMIN_PASSWORD = "test";

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    private ReviewSearchService reviewSearchService;
    @MockBean
    private CosmeticRankService cosmeticRankService;
    @MockBean
    private CosmeticSearchService cosmeticSearchService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenProvider tokenProvider;

    private String accessToken;
    private String userId;

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
    public void testGet_ReviewAnalyze() throws Exception {
        String expectedString = "Hello";
        when(reviewSearchService.analyzeText(anyString())).thenReturn(expectedString);

        mockMvc.perform(get("/data-view/review/analyze?text=" + expectedString)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testGet_CosmeticData() throws Exception {
        String expectedString = "Cosmetic";
        when(cosmeticSearchService.viewCosmeticsData()).thenReturn(expectedString);

        mockMvc.perform(get("/data-view/cosmetics")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testGet_CosmeticMetricData() throws Exception {
        String expectedString = "CosmeticMetric";
        when(cosmeticSearchService.viewCosmeticMetricsData()).thenReturn(expectedString);

        mockMvc.perform(get("/data-view/cosmetic-metrics")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testGet_CosmeticCounts() throws Exception {
        var metrics = List.of(new CosmeticMetricData(), new CosmeticMetricData());
        when(cosmeticRankService.getAllCosmeticCounts()).thenReturn(metrics);

        mockMvc.perform(get("/data-view/cosmetic-counts")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
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
