package app.beautyminder.controller.admin;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.User;
import app.beautyminder.dto.ReviewStatusUpdateRequest;
import app.beautyminder.dto.chat.ChatKickDTO;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.auth.TokenService;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class AdminApiControllerTest {

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(3);

    private static final String TEST_ADMIN_EMAIL = "admintest@gmail.com";
    private static final String TEST_ADMIN_PASSWORD = "test";
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private WebApplicationContext context;
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
//    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /admin/hello")
    public void testSayHello() throws Exception {
        // given
        String url = "/admin/hello";

        // when
        ResultActions result = mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().string("Hello admin"));
    }

    @Test
    @DisplayName("Test GET /admin/reviews")
    public void testGetAllReviews() throws Exception {
        // given
        String url = "/admin/reviews";
        int page = 0;
        int size = 10;

        // when
        ResultActions result = mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + accessToken)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(size))));
    }

    @Test
    @DisplayName("Test GET /admin/reviews/denied")
    public void testGetFilteredReviews() throws Exception {
        // given
        String url = "/admin/reviews/denied";

        // when
        ResultActions result = mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(any(Integer.class))));
    }

    @Test
    @DisplayName("Test PATCH /admin/reviews/{reviewId}/status")
    public void testUpdateReviewStatus() throws Exception {
        // given
        String reviewId = "654356e29e8ae29a336cccf5";
        ReviewStatusUpdateRequest request = new ReviewStatusUpdateRequest("denied");
        String requestBody = objectMapper.writeValueAsString(request);
        String url = "/admin/reviews/";

        // when
        ResultActions result = mockMvc.perform(patch(url + reviewId + "/status")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isOk());
        // Add additional assertions based on how your application handles this request
    }


    @Test
    @DisplayName("Test POST /admin/chat/kick")
    public void testKickUser() throws Exception {
        // given
        String url = "/admin/chat/kick";
        ChatKickDTO request = new ChatKickDTO("testUser");
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isOk());
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
