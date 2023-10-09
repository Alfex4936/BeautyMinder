package app.beautyminder.controller;

import app.beautyminder.config.jwt.JwtProperties;
import app.beautyminder.domain.User;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserApiControllerTest {

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
    private UserService userService;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    private String accessToken;
    private String refreshToken;


    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

//        Thread.sleep(1000);

//        userRepository.deleteUserByEmail("test@com");
//        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }

    @Order(1)
    @DisplayName("Test Registration")
    @Test
    public void testSignup() throws Exception {
        // given
        String url = "/user/signup";
        String requestBody = "{\"email\": \"test@com\", \"password\": \"1234\"}";

        // when
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@com"));
    }

    @Order(2)
    @DisplayName("Test Login")
    @Test
    public void testLogin() throws Exception {
        // given
        RequestBuilder requestBuilder = formLogin().user("email", "test@com").password("1234");

        // when
        // Perform login and capture the response
        MvcResult result = mockMvc.perform(requestBuilder)
                .andDo(print())

                // then
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XRT"))
                .andExpect(header().exists("Authorization"))
                .andReturn();

        // Use the access token and refresh token in other tests
        accessToken = Objects.requireNonNull(result.getResponse().getHeader("Authorization")).split(" ")[1];
        refreshToken = Objects.requireNonNull(result.getResponse().getCookie("XRT")).getValue();

        assert validToken(accessToken);
        assert validToken(refreshToken);
    }

    @Order(3)
    @DisplayName("Test Add Todo")
    @Test
    public void testAddTodo() throws Exception {
        // given
        String url = "/todo/add";

        Optional<User> optUser = userRepository.findByEmail("test@com");
        if (optUser.isEmpty()) {
            throw new Exception("Non existent user");
        }
        User user = optUser.get();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getId());
        payload.put("data", "2023-09-25");
        payload.put("morningTasks", Arrays.asList("밥 먹기", "세수 하기"));
        payload.put("dinnerTasks", new ArrayList<>());
        String requestBody = objectMapper.writeValueAsString(payload);

        // when
        mockMvc.perform(post(url)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Todo added successfully"));
    }

    @Order(4)
    @DisplayName("Delete a user")
    @Test
    public void testDeleteUser() throws Exception {
        // given
        Optional<User> optUser = userRepository.findByEmail("test@com");
        if (optUser.isEmpty()) {
            throw new Exception("Non existent user");
        }
        User user = optUser.get();

        // when
        mockMvc.perform(delete("/user/delete/" + user.getId())
                        .header("Authorization", "Bearer " + accessToken))

                // then
                .andExpect(status().isOk())
                .andExpect(content().string("a user is deleted successfully"));

        // Further verification: Check that the user and related data are actually deleted
        assertFalse(userRepository.existsById(user.getId()));
        // Add similar checks for Todo and RefreshToken if they are linked to the user
        assertFalse(refreshTokenRepository.findByUserId(user.getId()).isPresent());
    }

    @AfterEach
    public void cleanUp() {
        // Clean up logic to run after each test if needed
    }

    @AfterAll
    public static void finalCleanUp() {
        // Final cleanup logic to run after all tests
//        userService.deleteUserAndRelatedData();
    }

    public boolean validToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .parseClaimsJws(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
