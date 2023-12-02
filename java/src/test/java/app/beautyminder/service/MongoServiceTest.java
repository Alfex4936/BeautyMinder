package app.beautyminder.service;

import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class MongoServiceTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private MongoService mongoService;
    @Autowired
    private UserService userService;

    private String userId;

    @BeforeAll
    public void setUp() {
        AddUserRequest dto = new AddUserRequest();
        dto.setEmail("dummy@user");
        dto.setPassword("1234");
        dto.setNickname("nick");
        dto.setPhoneNumber("01013286549");

        var user = userService.saveUser(dto);
        userId = user.getId();
    }

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    public void testUpdateFields_Success() throws IOException {
        Map<String, Object> map = Map.of("baumann", "ABCD", "phoneNumber", "0313391234", "profileImage", "notgoodimage");
        var optUser = mongoService.updateFields(userId, map, User.class);
        assertTrue(optUser.isPresent(), "Couldn't find a user");
        var user = optUser.get();
        assertEquals(user.getBaumann(), "ABCD");
        assertEquals(user.getPhoneNumber(), "01013286549"); // didnt update
        assertNotEquals(user.getProfileImage(), "notgoodimage"); // didnt update
        assertTrue(mongoService.touch(User.class, userId, "lastLogin"));
    }

    @Test
    public void testUpdateFields_Fail() throws IOException {
        Map<String, Object> map = Map.of("baumann", "ABCD", "phoneNumber", "0313391234");

        // Not existing 24 hex char userId
        Assertions.assertThrows(ResponseStatusException.class, () -> mongoService.updateFields("654356e19e8ae29a336ccced", map, User.class));
    }

    @Test
    public void testCheckingReference() throws IOException {
        var sampleReview = "654356e19e8ae29a336ccced";

        // this user doesn't have pre-existing review
        assertFalse(mongoService.existsWithReference(Review.class, sampleReview, "user", userId));
    }

    @AfterEach
    public void cleanUp() {
        // Clea  up logic to run after each test if needed
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
