package app.beautyminder.controller.cosmetic;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.CosmeticExpiry;
import app.beautyminder.domain.User;
import app.beautyminder.dto.expiry.AddExpiryProduct;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticExpiryService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class CosmeticExpiryApiControllerTest {

    private static final String TEST_USER_EMAIL = "cosmeticexpiry@test.com";
    private static final String TEST_USER_PASSWORD = "test";
    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofMinutes(3);
    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(1);

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private CosmeticExpiryService cosmeticExpiryService;

    private String userId;
    private String accessToken;
    private String refreshToken;

    private AddExpiryProduct createDTO() {
        AddExpiryProduct dto = new AddExpiryProduct();
        dto.setBrandName("Brand");
        dto.setImageUrl("http://example.com/image.jpg");
        dto.setCosmeticId("cosmetic123");
        dto.setExpiryDate("2023-12-31");
        dto.setOpenedDate("2023-01-01");
        dto.setProductName("Product");
        dto.setExpiryRecognized(true);
        dto.setOpened(true);

        return dto;
    }

    private CosmeticExpiry createExpiry(String userId) {
        var dto = createDTO();

        return CosmeticExpiry.builder().id("testId").userId(userId).cosmeticId(dto.getCosmeticId()).expiryRecognized(false).opened(false).build();
    }

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @BeforeAll
    public void initialize() {
        AddUserRequest addUserRequest = new AddUserRequest();
        addUserRequest.setEmail(TEST_USER_EMAIL);
        addUserRequest.setPassword(TEST_USER_PASSWORD);

        User user = userService.saveUser(addUserRequest);
        userId = user.getId();
        accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
    }

    @Test
    @DisplayName("Test POST /expiry/create")
    public void testPostExpiry() throws Exception {
        // given
        var expiry = createExpiry(userId);
        var addExpiryProductDto = createDTO(); // Create DTO object
        String url = "/expiry/create";
        when(cosmeticExpiryService.createCosmeticExpiry(eq(userId), any(AddExpiryProduct.class))).thenReturn(expiry);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addExpiryProductDto))); // Include DTO in request body

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test GET /expiry")
    public void testGetAllExpiries() throws Exception {
        // given
        var expiries = List.of(createExpiry(userId), createExpiry((userId)));
        String url = "/expiry";
        when(cosmeticExpiryService.getAllCosmeticExpiriesByUserId(eq(userId))).thenReturn(expiries);

        // when
        ResultActions result = mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + accessToken));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test GET one /expiry")
    public void testGetOneExpiry() throws Exception {
        // given
        var expiry = createExpiry(userId);
        var expiryId = expiry.getId();
        String url = "/expiry/" + expiryId;
        when(cosmeticExpiryService.getCosmeticExpiry(eq(userId), eq(expiryId))).thenReturn(expiry);

        // when
        ResultActions result = mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + accessToken));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test GET /expiry/page")
    public void testGetAllExpiriesInPage() throws Exception {
        // given
        var expiries = List.of(createExpiry(userId), createExpiry((userId)));

        // Create a PageRequest
        PageRequest pageRequest = PageRequest.of(0, 3);

        // Create a Page object using PageImpl
        Page<CosmeticExpiry> cosmeticPage = new PageImpl<>(expiries, pageRequest, expiries.size());

        String url = "/expiry/page";

        when(cosmeticExpiryService.getPagedAllCosmeticExpiriesByUserId(eq(userId), Mockito.any(Pageable.class))).thenReturn(cosmeticPage);

        // when
        ResultActions result = mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + accessToken));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test PUT /expiry/{expiryId} SUCCESS")
    public void testPutExpiry_Success() throws Exception {
        // given
        var expiry = createExpiry(userId);
        String expiryId = expiry.getId();
        String url = "/expiry/" + expiryId; // Include expiryId in the URL

        // Prepare a map of updates
        Map<String, Object> updates = new HashMap<>();
        updates.put("someField", "newValue"); // Replace with actual fields to update

        when(cosmeticExpiryService.findByUserIdAndId(eq(userId), eq(expiryId))).thenReturn(Optional.of(expiry));
        when(cosmeticExpiryService.updateCosmeticExpiry(eq(expiryId), any(Map.class))).thenReturn(Optional.of(expiry)); // Mock the update

        // when
        ResultActions result = mockMvc.perform(put(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates))); // Include updates in request body

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test PUT /expiry/{expiryId} Fail")
    public void testPutExpiry_Fail() throws Exception {
        // given
        var expiry = createExpiry(userId);
        String expiryId = expiry.getId();
        String url = "/expiry/" + expiryId; // Include expiryId in the URL

        // Prepare a map of updates
        Map<String, Object> updates = new HashMap<>();
        updates.put("someField", "newValue"); // Replace with actual fields to update

        when(cosmeticExpiryService.findByUserIdAndId(eq(userId), eq(expiryId))).thenReturn(Optional.of(expiry));
        when(cosmeticExpiryService.updateCosmeticExpiry(eq(expiryId), any(Map.class))).thenReturn(Optional.empty()); // Mock the update

        // when
        ResultActions result = mockMvc.perform(put(url)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates))); // Include updates in request body

        // then
        result.andExpect(status().isInternalServerError()); // ResponseStatusException
    }


    @Test
    @DisplayName("Test DELETE one /expiry")
    public void testDeleteOneExpiry() throws Exception {
        // given
        var expiry = createExpiry(userId);
        var expiryId = expiry.getId();
        String url = "/expiry/" + expiryId;
        doNothing().when(cosmeticExpiryService).deleteCosmeticExpiry(eq(userId), eq(expiryId));

        // when
        ResultActions result = mockMvc.perform(delete(url)
                .header("Authorization", "Bearer " + accessToken));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test Get /expiry/filter")
    public void testGetFilter() throws Exception {
        // given
        String startDate = "2023-01-01";
        String endDate = "2023-12-31";
        List<CosmeticExpiry> expectedExpiries = List.of(createExpiry(userId));
        when(cosmeticExpiryService.filterCosmeticExpiries(eq(userId), eq(startDate), eq(endDate)))
                .thenReturn(expectedExpiries);

        String url = "/expiry/filter";

        // when
        ResultActions result = mockMvc.perform(get(url)
                .param("startDate", startDate)
                .param("endDate", endDate)
                .header("Authorization", "Bearer " + accessToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedExpiries.size()))); // Optional: Check the size of the returned list
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
