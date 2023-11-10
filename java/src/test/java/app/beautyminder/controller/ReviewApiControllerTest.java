package app.beautyminder.controller;

import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.dto.ReviewUpdateDTO;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.ReviewService;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReviewApiControllerTest {

    private static final String TEST_USER_EMAIL = "usertest@gmail.com";
    private static final String TEST_USER_PASSWORD = "test";
    private static final String REVIEW_JSON_TEMPLATE = "{\"content\":\"%s\",\"rating\":%d,\"cosmeticId\":\"%s\",\"userId\":\"%s\"}";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ReviewService reviewService;
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
        User user = userRepository.save(User.builder()
                .email(TEST_USER_EMAIL)
                .password(TEST_USER_PASSWORD)
                .build());

        userId = user.getId();
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
                "652cdc2d2bf53d0109d1e210",
                userId);

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
                        .file(image1)).andDo(print())

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
                        .file(image1)).andDo(print())

                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.rating").value(1))
                .andExpect(jsonPath("$.content").value("Spring Boot 업데이트"))
                .andExpect(jsonPath("$.images[*]", hasItem(containsString("ajou2.png"))));
    }

    @Test
    @Order(3)
    @DisplayName("Test Review deletion")
    public void testReviewDelete() throws Exception {
        // given
        String url = "/review/" + reviewId;

        // when
        mockMvc.perform(delete(url))

                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string("Review deleted successfully"));
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
            reviewService.deleteReview(reviewId);
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }
    }
}
