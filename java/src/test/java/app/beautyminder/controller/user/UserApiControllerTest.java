package app.beautyminder.controller.user;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.ResetPasswordRequest;
import app.beautyminder.dto.user.UpdatePasswordRequest;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.util.AssertionErrors.assertNotEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class UserApiControllerTest {

    private final String userEmail = "codecov@github.ci";
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private WebApplicationContext context;
    private String accessToken;
    private String refreshToken;
    private String passCodeToken;

    @AfterAll
    public static void finalCleanUp() {
        // Final cleanup logic to run after all tests
//        userService.deleteUserAndRelatedData();
    }

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Order(1)
    @DisplayName("Test User Registration (request)")
    @Test
    public void testSignUpRequest() throws Exception {
        // given
        String url = "/user/email-verification/request?email=" + userEmail;

        // when
        MvcResult result = mockMvc.perform(post(url))

                // then
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();

        // Convert the response content to a Map
        String jsonResponse = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        // Get the password from the response map using the appropriate nested keys
        passCodeToken = (String) responseMap.get("token");
    }

    @Order(2)
    @DisplayName("Test User Registration (verify)")
    @Test
    public void testSignUpVerification() throws Exception {
        // given
        String url = "/user/email-verification/verify?token=" + passCodeToken;

        // when
        mockMvc.perform(post(url))

                // then - nothing much happening
                .andExpect(status().is2xxSuccessful());
    }

    @Order(3)
    @DisplayName("Test User Registration (verify) Fail")
    @Test
    public void testSignUpVerification_Fail() throws Exception {
        // given
        String url = "/user/email-verification/verify?token=" + passCodeToken + "1";

        // when
        mockMvc.perform(post(url))

                // then - nothing much happening
                .andExpect(status().isBadRequest());
    }

    @Order(2)
    @DisplayName("Test User Registration Request Fail")
    @Test
    public void testSignUpRequest_FailByExistingUser() throws Exception {
        // given
        String url = "/user/email-verification/request?email=web@com";

        // when
        mockMvc.perform(post(url))

                // then - nothing much happening
                .andExpect(status().isBadRequest());
    }

    @Order(3)
    @DisplayName("Test User Registration not verified Fail")
    @Test
    public void testSignUp_ShouldFailByNotVerified() throws Exception {
        // given
        String url = "/user/signup";
        var map = new HashMap<>(Map.of("email", userEmail + "a"));
        map.put("password", "1234");

        String requestBody = objectMapper.writeValueAsString(map);  // Convert map to JSON string


        // when
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                // then
                .andExpect(status().isBadRequest());
    }

    @Order(3)
    @DisplayName("Test User Registration")
    @Test
    public void testSignup() throws Exception {
        // given
        String url = "/user/signup";

        var map = new HashMap<>(Map.of("email", userEmail));
        map.put("password", "1234");

        String requestBody = objectMapper.writeValueAsString(map);  // Convert map to JSON string

        // when
        MvcResult result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();

        // then
        // Convert the response content to a Map
        String jsonResponse = result.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        // Get the password from the response map using the appropriate nested keys
        String password = (String) ((Map<String, Object>) responseMap.get("user")).get("password");

        // Use a basic JUnit assertion to check that the password is not "1234"
        assertNotEquals("1234", password, "The password should not be '1234'");
    }

    @Order(4)
    @DisplayName("Request Forgot password via email")
    @Test
    public void testForgotEmail() throws Exception {
        // given
        final String url = "/user/forgot-password";

        Optional<User> optUser = userRepository.findByEmail(userEmail);
        if (optUser.isEmpty()) {
            throw new Exception("Non existent user");
        }
        User user = optUser.get();

        var map = Map.of("email", user.getEmail());
        String requestBody = objectMapper.writeValueAsString(map);

        // when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isOk());
    }

    @Order(5)
    @DisplayName("Test Login")
    @Test
    public void testLogin() throws Exception {
        // given
        RequestBuilder requestBuilder = formLogin().user("email", userEmail).password("1234");

        // when
        // Perform login and capture the response
        MvcResult result = mockMvc.perform(requestBuilder)
                .andDo(print()) // This will print the request and response which is useful for debugging.

                // then
                .andExpect(status().isOk()) // Check if the status is OK.
                .andExpect(cookie().exists("XRT")) // Check if the "XRT" cookie exists.
                .andExpect(header().exists("Authorization")) // Check if the "Authorization" header exists.
                .andReturn(); // Store the result for further assertions.

        // Extract tokens for further use in other tests or assertions
        String authorizationHeader = result.getResponse().getHeader("Authorization");
        assertNotNull(authorizationHeader, "Authorization header is missing");

        // Use the access token and refresh token in other tests
        accessToken = Objects.requireNonNull(result.getResponse().getHeader("Authorization")).split(" ")[1];
        refreshToken = Optional.ofNullable(result.getResponse().getCookie("XRT"))
                .map(Cookie::getValue)
                .orElseThrow(() -> new AssertionError("XRT cookie is missing"));
        log.info("BEMINDER: TEST login: {}", accessToken);

        assertTrue(tokenProvider.validToken(accessToken), "Access token is invalid");
        assertTrue(tokenProvider.validToken(refreshToken), "Refresh token is invalid");

        // Extract the userId from the accessToken and perform assertions
        String userId = tokenProvider.getUserId(accessToken);
        assertTrue(userRepository.findById(userId).isPresent(), "User ID from token does not exist in repository");
    }

    @Order(6)
    @DisplayName("Test /user/me")
    @Test
    public void testGetProfile() throws Exception {
        // given
        String url = "/user/me";
        Optional<User> optUser = userRepository.findByEmail(userEmail);
        if (optUser.isEmpty()) {
            throw new Exception("Non existent user");
        }
        User user = optUser.get();

        // when
        ResultActions result = mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()));
    }

    @Order(7)
    @Test
    @DisplayName("Test PATCH /user/update")
    public void testUpdateProfile() throws Exception {
        // given
        String url = "/user/update";
        Map<String, Object> updates = new HashMap<>();
        updates.put("phoneNumber", "01041369546");

        String requestBody = objectMapper.writeValueAsString(updates);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isOk()).andExpect(jsonPath("$.phoneNumber").value("01041369546"));
    }

    @Order(8)
    @Test
    @DisplayName("Test PATCH /user/update FAIL")
    public void testUpdateProfile_ShouldFailDuplicatedNumber() throws Exception {
        // given
        String url = "/user/update";
        Map<String, Object> updates = new HashMap<>();
        updates.put("phoneNumber", "01012345678");

        String requestBody = objectMapper.writeValueAsString(updates);

        // when
        ResultActions result = mockMvc.perform(patch(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isNotAcceptable());
    }

    @Order(8)
    @Test
    @DisplayName("Test POST /user/favorites/{cosmeticId}")
    public void testAddToUserFavorite() throws Exception {
        // given
        String cosmeticId = "652cdc2d2bf53d0109d1e210";
        String url = "/user/favorites/" + cosmeticId;

        // when
        ResultActions result = mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }

    @Order(9)
    @Test
    @DisplayName("Test DELETE /user/favorites/{cosmeticId}")
    public void testRemoveFromUserFavorite() throws Exception {
        // given
        String cosmeticId = "652cdc2d2bf53d0109d1e210";
        String url = "/user/favorites/" + cosmeticId;

        // when
        ResultActions result = mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }

    @Order(10)
    @Test
    @DisplayName("Test GET /user/favorites")
    public void testGetFavorites() throws Exception {
        // given
        String url = "/user/favorites";

        // when
        ResultActions result = mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }

    @Order(11)
    @Test
    @DisplayName("Test GET /user/reviews")
    public void testGetUserReviews() throws Exception {
        // given
        String url = "/user/reviews";

        // when
        ResultActions result = mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }

    @Order(12)
    @Test
    @DisplayName("Test POST /user/upload")
    public void testUploadProfileImage() throws Exception {
        // given
        String url = "/user/upload";
        File imageFile = new File("src/test/resources/ajou.png");
        MockMultipartFile image1 = new MockMultipartFile("image",
                "ajou2.png",
                "image/png",
                Files.readAllBytes(imageFile.toPath()));
        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                .file(image1)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().string(containsString(".png"))); // Expect the URL of the uploaded image
    }

    @Order(13)
    @Test
    @DisplayName("Test GET /user/search-history")
    public void testGetKeywordHistory() throws Exception {
        // given
        String url = "/user/search-history";

        // when
        ResultActions result = mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()); // Validate that the response body is an array
    }

    @Order(13)
    @Test
    @DisplayName("Test POST /update-password")
    public void testUpdatePassword() throws Exception {
        // given
        String url = "/user/update-password";
        UpdatePasswordRequest  request = new UpdatePasswordRequest("1234", "newPassword"); // invalid token
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().is2xxSuccessful());
    }

    @Order(14)
    @Test
    @DisplayName("Test POST /reset-password Bad Request")
    public void testResetPasswordBadRequest() throws Exception {
        // given
        String url = "/user/reset-password";
        ResetPasswordRequest request = new ResetPasswordRequest("", "newPassword"); // invalid token
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Order(15)
    @DisplayName("Logout a user")
    @Test
    public void testLogout() throws Exception {
        // given
        String url = "/user/logout";

        // when
        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + accessToken))

                // then
                .andExpect(status().isOk());

    }

    @Order(16)
    @DisplayName("Delete a user")
    @Test
    public void testDeleteUser() throws Exception {
        // given
        Optional<User> optUser = userRepository.findByEmail(userEmail);
        if (optUser.isEmpty()) {
            throw new Exception("Non existent user");
        }
        User user = optUser.get();

        log.info("BEMINDER: Test delete {}", accessToken);

        // when
        mockMvc.perform(delete("/user/delete")
                        .header("Authorization", "Bearer " + accessToken))

                // then
                .andExpect(status().isOk())
                .andExpect(content().string("a user is deleted successfully"));

        assertFalse(userRepository.existsById(user.getId()));
        assertFalse(refreshTokenRepository.findByUserId(user.getId()).isPresent());
    }

    @AfterEach
    public void cleanUp() {
        // Clean up logic to run after each test if needed
    }
}
