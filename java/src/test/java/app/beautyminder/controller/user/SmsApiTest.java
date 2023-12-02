package app.beautyminder.controller.user;

import app.beautyminder.domain.User;
import app.beautyminder.dto.PasswordResetResponse;
import app.beautyminder.dto.sms.SmsResponseDTO;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.service.auth.SmsService;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @MockBean
    private SmsService smsService;

    private String userId;

    private final String TEST_REAL_NUMBER = "01064647887";

    private User user;

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

        user = userService.saveAdmin(addUserRequest);
        userId = user.getId();
    }

    @Test
    @DisplayName("Test GET /user/sms/send")
    public void testSendSMS_Success() throws Exception {
        // Create a mock SmsResponseDTO
        SmsResponseDTO mockSmsResponseDTO = new SmsResponseDTO();

        // Mock the behavior of the SMS sending service
        Mockito.when(smsService.sendSms(Mockito.any())).thenReturn(mockSmsResponseDTO);

        // given
        String url = "/user/sms/send/" + TEST_REAL_NUMBER;

        // when
        ResultActions result = mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());

        // Verify that the SMS service was called with any PasswordResetResponse
        Mockito.verify(smsService).sendSms(Mockito.any(PasswordResetResponse.class));
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
