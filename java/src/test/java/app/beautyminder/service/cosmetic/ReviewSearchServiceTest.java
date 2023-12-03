package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.EsReview;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.repository.elastic.EsReviewRepository;
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
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class ReviewSearchServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private EsReviewRepository esReviewRepository;
    @Mock
    private RestHighLevelClient opensearchClient;

    private ObjectMapper objectMapper;

    @InjectMocks
    private ReviewSearchService reviewSearchService;

    private User createUser() {
        return User.builder().email("reviewsearch@test.com").password("1234").nickname("nick").build();
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
        reviewRepository = mock(ReviewRepository.class);
        esReviewRepository = mock(EsReviewRepository.class);
        opensearchClient = mock(RestHighLevelClient.class);
        objectMapper = new ObjectMapper();
        reviewSearchService = new ReviewSearchService(reviewRepository, esReviewRepository, opensearchClient, objectMapper);
    }

    @Test
    public void testIndexReviews() {
        // Setup
        var review = createReview();
        var review2 = createReview();
        List<Review> mockReviews = Arrays.asList(review, review2);
        when(reviewRepository.findAll()).thenReturn(mockReviews);

        // Execute
        reviewSearchService.indexReviews();

        // Verify
        verify(reviewRepository).findAll();
        verify(esReviewRepository).saveAll(anyList());
    }

    @Test
    public void testGetIndexOfReviews() throws IOException {
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
        String result = reviewSearchService.getIndexOfReviews();

        // Verify the results and interactions
        verify(mockLowLevelClient).performRequest(any(Request.class));
        assertEquals("expected response", result);
    }

    @Test
    public void testViewReviewsData() throws IOException {
        // Mock dependencies
        Response mockResponse = mock(Response.class); // Request request = new Request("GET", "/reviews/_search");
        HttpEntity mockEntity = mock(HttpEntity.class);
        RestClient mockLowLevelClient = mock(RestClient.class);
        when(opensearchClient.getLowLevelClient()).thenReturn(mockLowLevelClient);
        when(mockLowLevelClient.performRequest(any(Request.class))).thenReturn(mockResponse);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        // Mock the behavior of the entity to return a specific string content
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream("expected response".getBytes(StandardCharsets.UTF_8)));

        // Execute the method under test
        String result = reviewSearchService.viewReviewsData();

        // Verify the results and interactions
        verify(mockLowLevelClient).performRequest(any(Request.class));
        assertEquals("expected response", result);
    }

    @Test
    public void testAnalyzeText() throws IOException {
        // Mock dependencies
        Response mockResponse = mock(Response.class);
        HttpEntity mockEntity = mock(HttpEntity.class);
        RestClient mockLowLevelClient = mock(RestClient.class);
        when(opensearchClient.getLowLevelClient()).thenReturn(mockLowLevelClient);
        when(mockLowLevelClient.performRequest(any(Request.class))).thenReturn(mockResponse);
        when(mockResponse.getEntity()).thenReturn(mockEntity);

        // Mock the behavior of the entity
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream("analysis result".getBytes(StandardCharsets.UTF_8)));

        // Execute the method under test
        String result = reviewSearchService.analyzeText("sample text");

        // Verify the results and interactions
        verify(mockLowLevelClient).performRequest(argThat(request ->
                request.getMethod().equals("POST") && request.getEndpoint().equals("/reviews/_analyze")
        ));
        assertEquals("analysis result", result);
    }

    @Test
    public void testDelete() throws IOException {
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
        reviewSearchService.delete();
        verify(mockLowLevelClient).performRequest(argThat(request ->
                request.getMethod().equals("DELETE") && request.getEndpoint().equals("/reviews")
        ));

        // Test for unsuccessful deletion
        when(mockStatusLine.getStatusCode()).thenReturn(500); // Simulate an error
        reviewSearchService.delete();
    }

    @Test
    public void testSearchByContent_Exception() throws IOException {
        // Mock dependencies
        when(opensearchClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenThrow(new IOException("Test exception"));

        // Execute the method under test
        List<Review> result = reviewSearchService.searchByContent("sample content");

        // Verify the results and interactions
        assertTrue(result.isEmpty());
    }
}
