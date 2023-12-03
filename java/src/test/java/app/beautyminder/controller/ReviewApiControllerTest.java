package app.beautyminder.controller;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.dto.ReviewUpdateDTO;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles({"awsBasic", "test"})
class ReviewApiControllerTest {

    private static final String TEST_USER_EMAIL = "usertest@gmail.com";
    private static final String TEST_USER_PASSWORD = "test";
    private static final String REVIEW_JSON_TEMPLATE = "{\"content\":\"%s\",\"rating\":%d,\"cosmeticId\":\"%s\"}";
    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofMinutes(3);
    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(2);
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper; // Injected ObjectMapper
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenProvider tokenProvider;
    private String accessToken;
    private String refreshToken;

    private String userId;
    private String reviewId;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @BeforeAll
    public void initialize() {
        AddUserRequest addUserRequest = new AddUserRequest();
        addUserRequest.setEmail(TEST_USER_EMAIL);
        addUserRequest.setPassword(TEST_USER_PASSWORD);

        User user = userService.saveUser(addUserRequest);
        userId = user.getId();
        accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
    }

    @Test
    @Order(1)
    @DisplayName("Test Review create")
    public void testReviewCreate() throws Exception {
        // given
        String url = "/review";
        String reviewJson = String.format(REVIEW_JSON_TEMPLATE,
                "Spring Boot 테스트 파일...",
                3,
                "65576e4c8247c8f1003781bc"); // 테스트용 제품

        MockMultipartFile reviewFile = new MockMultipartFile("review",
                "",
                "application/json",
                reviewJson.getBytes(StandardCharsets.UTF_8));

        // Load the image file
        File imageFile = new File("src/test/resources/ajou.png"); // Ensure file path is correct
        MockMultipartFile image1 = new MockMultipartFile("images",
                "ajou.png",
                "image/png",
                Files.readAllBytes(imageFile.toPath()));

        // when
        MvcResult mvcResult = mockMvc.perform(multipart(url)
                        .file(reviewFile)
                        .file(image1)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())

                // then
                .andExpect(status().isCreated())
                .andReturn();

        TypeReference<Review> typeRef = new TypeReference<>() {
        };
        Review review = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), typeRef);
        reviewId = review.getId();
    }

    @Test
    @Order(2)
    @DisplayName("Test Review update")
    public void testReviewUpdate() throws Exception {
        // given
        String url = "/review/" + reviewId;

        ReviewUpdateDTO reviewUpdateDTO = new ReviewUpdateDTO();
        reviewUpdateDTO.setContent("Spring Boot 업데이트");
        reviewUpdateDTO.setRating(1);
        reviewUpdateDTO.setImagesToDelete(List.of("ajou.png"));

        String reviewJson = objectMapper.writeValueAsString(reviewUpdateDTO);

        MockMultipartFile updateFile = new MockMultipartFile("review",
                "",
                "application/json",
                reviewJson.getBytes(StandardCharsets.UTF_8));

        // Load the image file
        File imageFile = new File("src/test/resources/ajou.png");
        MockMultipartFile image1 = new MockMultipartFile("images",
                "ajou2.png",
                "image/png",
                Files.readAllBytes(imageFile.toPath()));

        // when
        mockMvc.perform(multipart(HttpMethod.PUT, url)
                        .file(updateFile)
                        .file(image1)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())

                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.rating").value(1))
                .andExpect(jsonPath("$.content").value("Spring Boot 업데이트"))
                .andExpect(jsonPath("$.images[*]", hasItem(containsString(".png")))); // uuid name
    }

    @Test
    @Order(3)
    @DisplayName("Test Review deletion")
    public void testReviewDelete() throws Exception {
        // given
        String url = "/review/" + reviewId;

        // when
        mockMvc.perform(delete(url)
                        .header("Authorization", "Bearer " + accessToken))

                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string("Review deleted successfully"));
    }

    @Test
    @Order(4)
    @DisplayName("Test Get All reviews")
    public void testGetAllReviews_Success() throws Exception {
        // given
        String url = "/review/" + "652cdc2d2bf53d0109d1e210";

        // when
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + accessToken))

                // then
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @Order(5)
    @DisplayName("Test Get All reviews Fail")
    public void testGetAllReviews_Fail() throws Exception {
        // given
        String url = "/review/" + "652cdc2d2bf53d0109d1e21Z";

        // when
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + accessToken))

                // then
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    @DisplayName("Test Load image Success")
    public void testLoadImage_Success() throws Exception {
        // given
        String url = "/review/image?filename=" + "default/default_user_profile.png";

        // when
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + accessToken))

                // then
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    @DisplayName("Test Load image Fail")
    public void testLoadImage_Fail() throws Exception {
        // given
        String url = "/review/image?filename=" + "default_user_profile.png"; // wrong key

        // when
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + accessToken))

                // then
                .andExpect(status().isInternalServerError());
    }

    @AfterEach
    public void tearDown() {
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
