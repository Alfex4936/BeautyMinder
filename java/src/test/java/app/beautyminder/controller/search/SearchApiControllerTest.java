package app.beautyminder.controller.search;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.User;
import app.beautyminder.dto.ReviewStatusUpdateRequest;
import app.beautyminder.dto.chat.ChatKickDTO;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.search.Search;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class SearchApiControllerTest {

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(3);

    private static final String TEST_USER_EMAIL = "searchtest@beminder.com";
    private static final String TEST_USER_PASSWORD = "test";
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

    @Autowired
    private SearchController searchController;

    private String accessToken;
    private String userId;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @BeforeAll
    public void initialize() {
        AddUserRequest addUserRequest = new AddUserRequest();
        addUserRequest.setEmail(TEST_USER_EMAIL);
        addUserRequest.setPassword(TEST_USER_PASSWORD);

        User user = userService.saveUser(addUserRequest);
        userId = user.getId();
        accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);

        searchController.updateUserSearchHistory(user, "setup");
    }

    @Test
    public void testSearchByName() throws Exception {
        // given
        String url = "/search/cosmetic";

        // when
        ResultActions result = mockMvc.perform(get(url)
                .param("name", "lipstick")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    public void testSearchByContentWithEmptyKeyword() throws Exception {
        // given
        String url = "/search/cosmetic";

        // when
        mockMvc.perform(get(url)
                .param("name", "")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    public void testSearchByCategory() throws Exception {
        String url = "/search/category";

        mockMvc.perform(get(url)
                .param("category", "스킨케어")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk());

        mockMvc.perform(get(url)
                .param("category", "")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSearchByReview() throws Exception {
        String url = "/search/review";

        mockMvc.perform(get(url)
                        .param("content", "스킨케어")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isOk());

        mockMvc.perform(get(url)
                        .param("content", "")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSearchByKeyword() throws Exception {
        String url = "/search/keyword";

        mockMvc.perform(get(url)
                .param("keyword", "#약산성")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mockMvc.perform(get(url)
                        .param("keyword", "")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSearchAnything() throws Exception {
        String url = "/search";

        mockMvc.perform(get(url)
                .param("anything", "파워10 젤리")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        mockMvc.perform(get(url)
                        .param("anything", "")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                // then
                .andExpect(status().isBadRequest());
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
