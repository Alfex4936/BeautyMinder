package app.beautyminder.service.log;

import app.beautyminder.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.*;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class LogServiceTest {

    private RestHighLevelClient mockClient;
    private RestClient mockLowLevelClient;
    private LogService logService;

    @BeforeEach
    void setUp() {
        mockClient = mock(RestHighLevelClient.class);
        mockLowLevelClient = mock(RestClient.class);
        when(mockClient.getLowLevelClient()).thenReturn(mockLowLevelClient);
        logService = new LogService(mockClient);
    }

    @Test
    void testGetTodaysLogs() throws IOException {
        // Arrange
        String todaysDate = LocalDate.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("YYYY.MM.dd"));
        String todaysIndex = "logstash-logs-" + todaysDate;

        SearchHit hit1 = mock(SearchHit.class);
        SearchHit hit2 = mock(SearchHit.class);
        when(hit1.getSourceAsString()).thenReturn("log1");
        when(hit2.getSourceAsString()).thenReturn("log2");

        SearchHit[] hitsArray = new SearchHit[]{hit1, hit2};
        SearchHits hits = mock(SearchHits.class);
        when(hits.getHits()).thenReturn(hitsArray);

        SearchResponse searchResponse = mock(SearchResponse.class);
        when(searchResponse.getHits()).thenReturn(hits);

        when(mockClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);

        // Act
        List<String> logs = logService.getTodaysLogs();

        // Assert
        assertEquals(2, logs.size());
        assertTrue(logs.contains("log1"));
        assertTrue(logs.contains("log2"));

        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(mockClient).search(requestCaptor.capture(), any(RequestOptions.class));
        assertEquals(todaysIndex, requestCaptor.getValue().indices()[0]);
    }

    @Test
    void testDeleteAllDocuments_Successful() throws IOException {
        // Arrange
        String indexName = "test-index";
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatusLine()).thenReturn(mock(StatusLine.class));
        when(mockResponse.getStatusLine().getStatusCode()).thenReturn(200);

        when(mockLowLevelClient.performRequest(any(Request.class))).thenReturn(mockResponse);

        // Act & Assert
        assertDoesNotThrow(() -> logService.deleteAllDocuments(indexName));
    }

    @Test
    void testDeleteAllDocuments_Failure() throws IOException {
        // Arrange
        String indexName = "test-index";
        Response mockResponse = mock(Response.class);
        StatusLine mockStatusLine = mock(StatusLine.class);

        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(500);

        when(mockLowLevelClient.performRequest(any(Request.class))).thenReturn(mockResponse);


        // Act & Assert
        try {
            logService.deleteAllDocuments(indexName);
            fail("Expected a ResponseStatusException to be thrown");
        } catch (IllegalArgumentException e) {
            // Expected, test passes
        } catch (Exception e) {
            fail("Unexpected exception type thrown: " + e.getClass().getName());
        }
    }

    @Test
    void testDeleteAllDocuments_IOException() throws IOException {
        // Arrange
        String indexName = "test-index";
        when(mockLowLevelClient.performRequest(any(Request.class))).thenThrow(new IOException());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> logService.deleteAllDocuments(indexName));
    }
}
