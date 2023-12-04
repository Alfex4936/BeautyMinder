package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.elastic.EsCosmeticRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class CosmeticSearchServiceTest {

    @Mock
    private CosmeticRepository cosmeticRepository;
    @Mock
    private EsCosmeticRepository esCosmeticRepository;
    @Mock
    private RestHighLevelClient opensearchClient;

    private ObjectMapper objectMapper;

    @InjectMocks
    private CosmeticSearchService cosmeticSearchService;

    private User createUser() {
        return User.builder().email("cosmeticsearch@test.com").password("1234").nickname("nick").build();
    }


    private Cosmetic createTestCosmetic() {
        LocalDate expirationDate = LocalDate.now().plusYears(1);
        LocalDate purchasedDate = LocalDate.now().minusDays(30);
        return Cosmetic.builder().name("Test Cosmetic").brand("Test Brand").expirationDate(expirationDate).purchasedDate(purchasedDate).category("Test Category").reviewCount(0).totalRating(0).favCount(0).build();
    }

    private Review createReview() {
        return Review.builder().id(UUID.randomUUID().toString()).content("test1").rating(3).user(createUser()).cosmetic(createTestCosmetic()).build();
    }

    @BeforeEach
    public void setup() {
        cosmeticRepository = mock(CosmeticRepository.class);
        esCosmeticRepository = mock(EsCosmeticRepository.class);
        opensearchClient = mock(RestHighLevelClient.class);
        objectMapper = new ObjectMapper();
        cosmeticSearchService = new CosmeticSearchService(cosmeticRepository, esCosmeticRepository, opensearchClient, objectMapper);
    }

    @Test
    public void testIndexCosmetics() {
        // Setup
        var cosmetic = createTestCosmetic();
        var cosmetic2 = createTestCosmetic();
        List<Cosmetic> mockCosmetics = Arrays.asList(cosmetic, cosmetic2);
        when(cosmeticRepository.findAll()).thenReturn(mockCosmetics);

        // Execute
        cosmeticSearchService.indexCosmetics();

        // Verify
        verify(cosmeticRepository).findAll();
        verify(esCosmeticRepository).saveAll(anyList());
    }

    @Test
    public void testListIndexOfCosmetics() throws IOException {
        // Mock dependencies
        Response mockResponse = mock(Response.class); // Request request = new Request("GET", "/reviews");
        HttpEntity mockEntity = mock(HttpEntity.class);
        RestClient mockLowLevelClient = mock(RestClient.class);
        when(opensearchClient.getLowLevelClient()).thenReturn(mockLowLevelClient);
        when(mockLowLevelClient.performRequest(any(Request.class))).thenReturn(mockResponse);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        // Mock the behavior of the entity to return a specific string content
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream("expected response".getBytes(StandardCharsets.UTF_8)));

        // Execute the method under test
        String result = cosmeticSearchService.listAllIndices();
        assertEquals("expected response", result);

        // Verify the results and interactions
        verify(mockLowLevelClient, times(1)).performRequest(any(Request.class));
    }

    @Test
    public void testGetIndexOfCosmetics() throws IOException {
        // Mock dependencies
        Response mockResponse = mock(Response.class); // Request request = new Request("GET", "/reviews");
        HttpEntity mockEntity = mock(HttpEntity.class);
        RestClient mockLowLevelClient = mock(RestClient.class);
        when(opensearchClient.getLowLevelClient()).thenReturn(mockLowLevelClient);
        when(mockLowLevelClient.performRequest(any(Request.class))).thenReturn(mockResponse);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        // Mock the behavior of the entity to return a specific string content
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream("expected response".getBytes(StandardCharsets.UTF_8)));

        // Execute the method under test
        String result = cosmeticSearchService.getIndexOfCosmetics();
        assertEquals("expected response", result);

        // Verify the results and interactions
        verify(mockLowLevelClient, times(1)).performRequest(any(Request.class));
    }

    @Test
    public void testViewCosmeticData() throws IOException {
        // Mock dependencies
        Response mockResponse = mock(Response.class); // Request request = new Request("GET", "/reviews");
        HttpEntity mockEntity = mock(HttpEntity.class);
        RestClient mockLowLevelClient = mock(RestClient.class);
        when(opensearchClient.getLowLevelClient()).thenReturn(mockLowLevelClient);
        when(mockLowLevelClient.performRequest(any(Request.class))).thenReturn(mockResponse);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        // Mock the behavior of the entity to return a specific string content
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream("expected response".getBytes(StandardCharsets.UTF_8)));

        // Execute the method under test
        String result = cosmeticSearchService.viewCosmeticsData();
        assertEquals("expected response", result);

        // Verify the results and interactions
        verify(mockLowLevelClient, times(1)).performRequest(any(Request.class));
    }

    @Test
    public void testViewCosmeticMetric() throws IOException {
        // Mock dependencies
        Response mockResponse = mock(Response.class); // Request request = new Request("GET", "/reviews");
        HttpEntity mockEntity = mock(HttpEntity.class);
        RestClient mockLowLevelClient = mock(RestClient.class);
        when(opensearchClient.getLowLevelClient()).thenReturn(mockLowLevelClient);
        when(mockLowLevelClient.performRequest(any(Request.class))).thenReturn(mockResponse);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        // Mock the behavior of the entity to return a specific string content
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream("expected response".getBytes(StandardCharsets.UTF_8)));

        // Execute the method under test
        String result = cosmeticSearchService.viewCosmeticMetricsData();
        assertEquals("expected response", result);

        // Verify the results and interactions
        verify(mockLowLevelClient, times(1)).performRequest(any(Request.class));
    }

    @Test
    public void testDeleteCosmeticIndex() throws IOException {
        // Mock dependencies
        Response mockResponse = mock(Response.class);
        StatusLine mockStatusLine = mock(StatusLine.class);
        RestClient mockLowLevelClient = mock(RestClient.class);
        HttpEntity mockEntity = mock(HttpEntity.class);

        when(opensearchClient.getLowLevelClient()).thenReturn(mockLowLevelClient);
        when(mockLowLevelClient.performRequest(any(Request.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockResponse.getEntity()).thenReturn(mockEntity);

        // Mock the behavior of the entity
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream("delete result".getBytes(StandardCharsets.UTF_8)));

        // Test for successful deletion
        when(mockStatusLine.getStatusCode()).thenReturn(200);
        cosmeticSearchService.delete();
        verify(mockLowLevelClient).performRequest(argThat(request ->
                request.getMethod().equals("DELETE") && request.getEndpoint().equals("/cosmetics")
        ));

        // Test for unsuccessful deletion
        when(mockStatusLine.getStatusCode()).thenReturn(500); // Simulate an error
        cosmeticSearchService.delete();
    }

}
