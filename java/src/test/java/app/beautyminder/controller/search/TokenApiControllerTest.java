package app.beautyminder.controller.search;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.controller.user.TokenController;
import app.beautyminder.domain.RefreshToken;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.service.auth.RefreshTokenService;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.LocalDateTime;

import static app.beautyminder.config.WebSecurityConfig.REFRESH_TOKEN_COOKIE_NAME;
import static app.beautyminder.config.WebSecurityConfig.REFRESH_TOKEN_DURATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class TokenApiControllerTest {

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(3);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User user;
    private String accessToken;
    private String refreshToken;

    @BeforeAll
    public void beforeAll() {
        MockitoAnnotations.openMocks(this);

        AddUserRequest addUserRequest = new AddUserRequest();
        addUserRequest.setEmail("springtoken@test");
        addUserRequest.setPassword("1234");

        user = userService.saveUser(addUserRequest);

        accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);

        LocalDateTime expiresAt = LocalDateTime.now().plus(REFRESH_TOKEN_DURATION);

        RefreshToken dbToken = refreshTokenRepository.findByUserId(user.getId()).map(entity -> {
            entity.update(refreshToken);
            entity.setExpiresAt(expiresAt);
            return entity;
        }).orElse(new RefreshToken(user, refreshToken, expiresAt));

        refreshTokenRepository.save(dbToken);
    }

    @Test
    public void testRefreshTokenSuccess() throws Exception {
        // given
        String url = "/token/refresh";

        // Create a cookie with the refresh token
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .cookie(refreshTokenCookie)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.accessToken").exists());
        result.andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    public void testRefreshTokenFail() throws Exception {
        // given
        String url = "/token/refresh";

        // Create a cookie with an invalid refresh token
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken + "A");

        // when
        ResultActions result = mockMvc.perform(post(url)
                .cookie(refreshTokenCookie)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @AfterAll
    public void cleanUpAll() {
        try {
            // Final cleanup logic to run after all tests
            userService.deleteUserAndRelatedData(user.getId());
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }
    }
}
