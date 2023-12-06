package app.beautyminder.controller.recommend;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.service.MongoService;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class RecommendApiControllerTest {

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(3);

    private static final String TEST_USER_EMAIL = "recommendtest@beminder.com";
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
    private MongoService mongoService;

    private String accessToken;
    private String userId;
    private List<Cosmetic> firstRecommendations;

    @BeforeEach
    public void beforeEach() {
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

        userService.addCosmeticById(userId, "652cdc2d2bf53d0109d1e210");
        mongoService.updateFields(userId, Map.of("baumann", "OSNT"), User.class);
    }

    @Test
    @Order(1)
    public void testRepeatedRecommendationsAreSame() throws Exception {
        // given
        String url = "/recommend";

        // when
        MvcResult firstResult = mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // then
        String firstResponseContent = firstResult.getResponse().getContentAsString();
        firstRecommendations = objectMapper.readValue(firstResponseContent, new TypeReference<>() {
        });

        // when again
        MvcResult secondResult = mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // then again
        String secondResponseContent = secondResult.getResponse().getContentAsString();
        List<Cosmetic> secondRecommendations = objectMapper.readValue(secondResponseContent, new TypeReference<>() {
        });

        // assert both recommendations are the same
        assertEquals(firstRecommendations, secondRecommendations, "Recommendations should be the same when fetched in quick succession");
    }

    // a bit random so it might be same actually.
//    @Test
//    @Order(2)
//    public void testRecommendationWithoutFav() throws Exception {
//        // given
//        String url = "/recommend";
//        userService.removeCosmeticById(userId, "652cdc2d2bf53d0109d1e210"); // empty fav list
//
//        // when
//        MvcResult updatedResult = mockMvc.perform(get(url)
//                        .header("Authorization", "Bearer " + accessToken))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn();
//        // then
//        String updatedResponseContent = updatedResult.getResponse().getContentAsString();
//        List<Cosmetic> updatedRecommendations = objectMapper.readValue(updatedResponseContent, new TypeReference<>() {
//        });
//
//        // when again with forceRefresh
//        MvcResult secondResult = mockMvc.perform(get(url + "?refresh=true")
//
//                        .header("Authorization", "Bearer " + accessToken))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn();
//
//        // then again
//        String secondResponseContent = secondResult.getResponse().getContentAsString();
//        List<Cosmetic> secondRecommendations = objectMapper.readValue(secondResponseContent, new TypeReference<>() {
//        });
//
//        // assert that recommendations have changed after user update
//        assertNotEquals(firstRecommendations, updatedRecommendations, "Recommendations should change after user information is updated");
//        assertNotEquals(secondRecommendations, updatedRecommendations, "Recommendations should change after force refresh cache");
//    }

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
