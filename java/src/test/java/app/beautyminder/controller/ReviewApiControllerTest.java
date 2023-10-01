package app.beautyminder.controller;

import app.beautyminder.config.jwt.JwtProperties;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.auth.UserService;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileInputStream;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReviewApiControllerTest {

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



    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Order(1)
    @DisplayName("Test Review add")
    @Test
    public void testUpload() throws Exception {
        // given
        String url = "/review/new";
        String reviewJson = "{" +
                "\"title\":\"Great product\"," +
                "\"content\":\"Really enjoyed using this product...\"," +
                "\"rating\":3" +
                "}";
        MockMultipartFile review = new MockMultipartFile("review", "", "application/json", reviewJson.getBytes());

        // Load the ajou.png file
        File file = new File("src/test/resources/ajou.png");  // Adjust the file path as needed
        FileInputStream input = new FileInputStream(file);
        MockMultipartFile image1 = new MockMultipartFile("images", "ajou.png", "image/png", IOUtils.toByteArray(input));

        // when
        mockMvc.perform(multipart(url)
                        .file(review)
                        .file(image1))

                // then
                .andExpect(status().isOk());
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
}
