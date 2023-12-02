package app.beautyminder.controller.elasticsearch;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.User;
import app.beautyminder.dto.CosmeticMetricData;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.service.LogService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
public class EsIndexApiControllerTest {

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(3);

    private static final String TEST_ADMIN_EMAIL = "esindexadmin@test.com";
    private static final String TEST_ADMIN_PASSWORD = "test";

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    private LogService logService;
    @MockBean
    private ReviewSearchService reviewSearchService;
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
    public void testGet_CosmeticsIndices() throws Exception {
        String expectedString = "indices";
        when(cosmeticSearchService.listAllIndices()).thenReturn(expectedString);

        mockMvc.perform(get("/es-index/cosmetics/list")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testGet_CosmeticsData() throws Exception {
        String expectedString = "cosmetics";
        when(cosmeticSearchService.getIndexOfCosmetics()).thenReturn(expectedString);

        mockMvc.perform(get("/es-index/cosmetics")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testDelete_CosmeticIndices() throws Exception {
        doNothing().when(logService).deleteAllDocuments("cosmetics-test");

        mockMvc.perform(delete("/es-index/cosmetics/delete")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testDelete_ReviewIndices() throws Exception {
        doNothing().when(logService).deleteAllDocuments("reviews-test");

        mockMvc.perform(delete("/es-index/reviews/delete")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testPost_CosmeticsIndex() throws Exception {
        doNothing().when(cosmeticSearchService).indexCosmetics();

        mockMvc.perform(post("/es-index/cosmetics/index")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testPost_ReviewsIndex() throws Exception {
        doNothing().when(reviewSearchService).indexReviews();

        mockMvc.perform(post("/es-index/reviews/index")
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
