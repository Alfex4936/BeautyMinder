package app.beautyminder.controller.user;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.MongoService;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"awsBasic", "test"})
class UserApiFailControllerTest {

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(3);

    private static final String TEST_USER_EMAIL = "userfailtest@gmail.com";
    private static final String TEST_USER_PASSWORD = "test";

    @Autowired
    protected MockMvc mockMvc;
    String userId;
    String accessToken;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TokenProvider tokenProvider;

    @MockBean
    private UserService userService;

    @MockBean
    private MongoService mongoService;
    @MockBean
    private ReviewRepository reviewRepository;
    @MockBean
    private UserRepository userRepository;

    private User user;

    @BeforeAll
    public void initialize() {
        user = User.builder().email(TEST_USER_EMAIL).password(TEST_USER_PASSWORD).build();
        accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
    }

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @DisplayName("Test User Registration - Exception in User Creation")
    @Test
    public void testSignUpExceptionInUserCreation() throws Exception {
        // given
        String url = "/user/signup";
        var map = new HashMap<>(Map.of("email", "fail", "password", "1234"));
        String requestBody = objectMapper.writeValueAsString(map); // Convert map to JSON string

        // Mock UserService to throw an exception for saveUser
        Mockito.when(userService.saveUser(any(AddUserRequest.class))).thenThrow(new RuntimeException("Test Exception"));

        // when
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                // then
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Test User Update - Not Valid phoneNumber")
    @Test
    public void testUpdateUserExceptionInPhoneNumberValidation() throws Exception {
        // given
        String url = "/user/update";
        String phoneNumber = "0103334444";
        var map = new HashMap<>(Map.of("phoneNumber", phoneNumber)); // invalid 7 digits
        String requestBody = objectMapper.writeValueAsString(map); // Convert map to JSON string

        // Mock UserService to throw an exception for saveUser
        Mockito.when(mongoService.isValidKoreanPhoneNumber(eq(phoneNumber)))
                .thenReturn(false);
        Mockito.when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));

        // when
        mockMvc.perform(patch(url)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON).content(requestBody))
                // then
                .andExpect(status().isNotAcceptable());
    }

    @DisplayName("Test User Reviews - Exception in reviews")
    @Test
    public void testGetReviewsByUserException() throws Exception {
        // given
        String url = "/user/reviews";

        // Mock UserService to throw an exception for saveUser
        Mockito.when(reviewRepository.findByUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Test Exception"));
        Mockito.when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(user));


        // when
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + accessToken))
                // then
                .andExpect(status().isBadRequest());
    }

    @AfterEach
    public void cleanUp() {
        // Clean up logic to run after each test if needed
    }

    @AfterAll
    public void cleanUpAll() {
//        try {
//            // Final cleanup logic to run after all tests
//            userService.deleteUserAndRelatedData(userId);
//        } catch (Exception e) {
//            System.err.println("Cleanup failed: " + e.getMessage());
//        }
    }
}
