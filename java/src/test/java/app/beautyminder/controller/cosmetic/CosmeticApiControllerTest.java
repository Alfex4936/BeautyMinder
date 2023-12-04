package app.beautyminder.controller.cosmetic;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class CosmeticApiControllerTest {

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(3);

    private static final String TEST_ADMIN_EMAIL = "cosmeticadmin@beautyminder.com";
    private static final String TEST_ADMIN_PASSWORD = "test";

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenProvider tokenProvider;
    private String accessToken;
    private String userId;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private CosmeticService cosmeticService;

    private Cosmetic createTestCosmetic() {
        LocalDate expirationDate = LocalDate.now().plusYears(1);
        LocalDate purchasedDate = LocalDate.now().minusDays(30);
        return Cosmetic.builder().id("test").name("Test Cosmetic").brand("Test Brand").expirationDate(expirationDate).purchasedDate(purchasedDate).category("Test Category").reviewCount(0).totalRating(0).favCount(0).build();
    }

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @BeforeAll
    public void initialize() {
        AddUserRequest addUserRequest = new AddUserRequest();
        addUserRequest.setEmail(TEST_ADMIN_EMAIL);
        addUserRequest.setPassword(TEST_ADMIN_PASSWORD);

        User user = userService.saveAdmin(addUserRequest);
        userId = user.getId();
        accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
    }

    @Test
    @DisplayName("Test GET /cosmetic")
    public void testGetAllCosmetics() throws Exception {
        // given
        var cosmetics = List.of(createTestCosmetic(), createTestCosmetic());
        String url = "/cosmetic";
        Mockito.when(cosmeticService.getAllCosmetics()).thenReturn(cosmetics);

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2))) // Check if the JSON array has 2 items
                .andExpect(jsonPath("$[0].name", is(cosmetics.get(0).getName())))
                .andExpect(jsonPath("$[1].name", is(cosmetics.get(1).getName())));
    }

    @Test
    @DisplayName("Test GET /cosmetic/page")
    public void testGetAllCosmeticsInPage() throws Exception {
        // given
        // Create a list of Cosmetic objects
        List<Cosmetic> cosmetics = List.of(createTestCosmetic(), createTestCosmetic(), createTestCosmetic());

        // Create a PageRequest
        // a PageRequest for the first page (page 0) with a size of 3.
        PageRequest pageRequest = PageRequest.of(0, 3);

        // Create a Page object using PageImpl
        Page<Cosmetic> cosmeticPage = new PageImpl<>(cosmetics, pageRequest, cosmetics.size());

        String url = "/cosmetic/page";

        // when
        Mockito.when(cosmeticService.getAllCosmeticsInPage(any(Pageable.class))).thenReturn(cosmeticPage);
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    @DisplayName("Test GET /cosmetic/{id} OK")
    public void testGetCosmetic_Success() throws Exception {
        // given
        Cosmetic cosmetic = createTestCosmetic();
        String cosmeticId = cosmetic.getId();
        String url = "/cosmetic/" + cosmeticId;
        Mockito.when(cosmeticService.getCosmeticById(cosmeticId)).thenReturn(cosmetic);

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test GET /cosmetic/{id} FAIL")
    public void testGetCosmetic_Fail() throws Exception {
        // given
        String url = "/cosmetic/" + "random";
        Mockito.when(cosmeticService.getCosmeticById("random")).thenReturn(null);

        // when
        ResultActions result = mockMvc.perform(get(url));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test GET /cosmetic/click and hit")
    public void testRankCosmeticIncrement() throws Exception {
        // given
        String cosmeticId = "652cdc2d2bf53d0109d1e210";
        String url = "/cosmetic/";

        // when
        mockMvc.perform(post(url + "click/" + cosmeticId))
                // then
                .andExpect(status().isOk());

        // when
        mockMvc.perform(post(url + "hit/" + cosmeticId))
                // then
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("Test CREATE cosmetic")
    public void testCreateCosmetic() throws Exception {
        // given
        var cosmetic = createTestCosmetic();
        String url = "/cosmetic";
        Mockito.when(cosmeticService.saveCosmetic(any(Cosmetic.class))).thenReturn(cosmetic);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cosmetic)));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test UPDATE cosmetic")
    public void testUpdateCosmetic() throws Exception {
        // given
        var cosmetic = createTestCosmetic();
        String url = "/cosmetic/" + cosmetic.getId();
        Mockito.when(cosmeticService.updateCosmetic(eq(cosmetic.getId()), any(Cosmetic.class))).thenReturn(cosmetic);

        // when
        ResultActions result = mockMvc.perform(put(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cosmetic)));

        // then
        result.andExpect(status().isOk());
    }
    @Test
    @DisplayName("Test UPDATE cosmetic FAIL")
    public void testUpdateCosmetic_ShouldFail() throws Exception {
        // given
        var cosmetic = createTestCosmetic();
        String url = "/cosmetic/" + cosmetic.getId();
        Mockito.when(cosmeticService.updateCosmetic(eq(cosmetic.getId()), any(Cosmetic.class))).thenReturn(null);

        // when
        ResultActions result = mockMvc.perform(put(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cosmetic)));

        // then
        result.andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("Test DELETE cosmetic")
    public void testDeleteCosmetic() throws Exception {
        // given
        var cosmetic = createTestCosmetic();
        String url = "/cosmetic/" + cosmetic.getId();
        Mockito.when(cosmeticService.deleteCosmetic(eq(cosmetic.getId()))).thenReturn(true);

        // when
        ResultActions result = mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + accessToken));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test DELETE cosmetic FAIL")
    public void testDeleteCosmetic_ShouldFail() throws Exception {
        // given
        var cosmetic = createTestCosmetic();
        String url = "/cosmetic/" + cosmetic.getId();
        Mockito.when(cosmeticService.deleteCosmetic(eq(cosmetic.getId()))).thenReturn(false);

        // when
        ResultActions result = mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + accessToken));

        // then
        result.andExpect(status().isNotFound());
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
