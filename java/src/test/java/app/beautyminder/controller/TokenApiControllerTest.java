package app.beautyminder.controller;

import app.beautyminder.config.jwt.JwtFactory;
import app.beautyminder.config.jwt.JwtProperties;
import app.beautyminder.domain.RefreshToken;
import app.beautyminder.domain.User;
import app.beautyminder.dto.CreateAccessTokenRequest;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.auth.TokenService;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class TokenApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    TokenService tokenService;

    String userId;
    User user;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    @DisplayName("createNewAccessToken: 새로운 액세스 토큰을 발급한다.")
    @Test
    public void createNewAccessToken() throws Exception {
        // given
        final String url = "/api/token";

        String refreshToken = JwtFactory.builder()
                .claims(Map.of("id", userId))
                .build()
                .createToken(jwtProperties);

        refreshTokenRepository.save(new RefreshToken(user, refreshToken));

        CreateAccessTokenRequest request = new CreateAccessTokenRequest();
        request.setRefreshToken(refreshToken);
        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @DisplayName("비밀번호 찾기용 토큰 생성 및 확인")
    @Test
    public void validatePasswordToken() throws Exception {
        // given
        final String url = "/user/forgot-password";

        var map = Map.of("email", user.getEmail());
        String requestBody = objectMapper.writeValueAsString(map);  // Convert map to JSON string


        // when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset email sent"));
    }

    @BeforeEach
    public void runUp() {
        user = userRepository.save(User.builder()
                .email("user@gmail.com")
                .password("test")
                .build());

        userId = user.getId();
    }

    @AfterEach
    public void cleanUp() {
        userService.deleteUserAndRelatedData(userId);
    }
}