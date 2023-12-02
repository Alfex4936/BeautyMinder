package app.beautyminder.controller.user;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.User;
import app.beautyminder.dto.ReviewStatusUpdateRequest;
import app.beautyminder.dto.chat.ChatKickDTO;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
class SmsApiTest {

    private static final String TEST_ADMIN_EMAIL = "sms@test";
    private static final String TEST_ADMIN_PASSWORD = "test";

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private WebApplicationContext context;
    private String userId;

    @Value("${naver.cloud.sms.sender-phone}")
    private String TEST_REAL_NUMBER;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @BeforeAll
    public void initialize() {
        AddUserRequest addUserRequest = new AddUserRequest();
        addUserRequest.setEmail(TEST_ADMIN_EMAIL);
        addUserRequest.setPassword(TEST_ADMIN_PASSWORD);
        addUserRequest.setPhoneNumber(TEST_REAL_NUMBER);

        User user = userService.saveAdmin(addUserRequest);
        userId = user.getId();
    }

    @Test
//    @WithMockUser(roles = "ADMIN")
    @DisplayName("Test GET /user/sms/send")
    public void testSendSMS_Success() throws Exception {
        // given
        String url = "/user/sms/send/" + TEST_REAL_NUMBER;

        // when
        ResultActions result = mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test failure GET 1 /user/sms/send")
    public void testSendSMS_NoNumber_Fail() throws Exception {
        // given
        String url = "/user/sms/send/ ";

        // when
        ResultActions result = mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Test failure GET 2 /user/sms/send")
    public void testSendSMS_WrongNumber_Fail() throws Exception {
        // given
        String url = "/user/sms/send/" + "0101234"; // wrong number

        // when
        ResultActions result = mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isInternalServerError()); // cannot find such user
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
