package app.beautyminder.controller;

import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.LocalFileService;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BaumannApiControllerTest {

    private static final String TEST_USER_EMAIL = "usertest@gmail.com";
    private static final String TEST_USER_PASSWORD = "test";

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
    private LocalFileService localFileService;
    private String userId;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @BeforeAll
    public void initialize() {
        AddUserRequest addUserRequest = new AddUserRequest();
        addUserRequest.setEmail(TEST_USER_EMAIL);
        addUserRequest.setPassword(TEST_USER_PASSWORD);

        userId = userService.saveUser(addUserRequest).getId();
    }

    @Test
    @Order(1)
    @DisplayName("Test Baumann Survey")
    public void testGetBaumannSurvey() throws Exception {
        // given
        String url = "/baumann/survey";

        // when
        MvcResult mvcResult = mockMvc.perform(get(url))
                // then
                .andExpect(status().is2xxSuccessful()).andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Check if the JSON object has 62 keys
        assertEquals("62 questions exist", 62, rootNode.size());
    }

    @Test
    @Order(2)
    @DisplayName("Test Baumann Update")
    public void testDoBaumann() throws Exception {
        // given
        String url = "/baumann/test/" + userId;

        JsonNode jsonObject = localFileService.readJsonFile("classpath:baumannMySurvey.json");
        String requestBody = objectMapper.writeValueAsString(jsonObject);

        // when
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                // then
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.skinType").value("OSNT"))
                .andExpect(jsonPath("$.scores.pigmentation").value(20.5));

        assertEquals("Baumann type must be", userRepository.findById(userId).get().getBaumann(), "OSNT");

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
