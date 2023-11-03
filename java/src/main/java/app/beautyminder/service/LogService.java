package app.beautyminder.service;

import app.beautyminder.domain.EsReview;
import app.beautyminder.domain.Review;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.repository.elastic.EsReviewRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.Request;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Response;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.FuzzyQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class LogService {

    private final RestHighLevelClient opensearchClient;

    public List<String> getTodaysLogs() throws IOException {
        // Format today's date as "YYYY.MM.dd"
        String todaysDate = LocalDate.now().format(DateTimeFormatter.ofPattern("YYYY.MM.dd"));
        // Construct the index name
        String todaysIndex = "logstash-logs-" + todaysDate;

        // Build your query
        SearchRequest searchRequest = new SearchRequest(todaysIndex);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);

        // Execute the search request
        SearchResponse searchResponse = opensearchClient.search(searchRequest, RequestOptions.DEFAULT);

        // Collect and return the logs
        return Arrays.stream(searchResponse.getHits().getHits())
                .map(SearchHit::getSourceAsString)
                .collect(Collectors.toList());
    }


    public void deleteAllDocuments(String indexName) {
        try {
            Request request = new Request("POST", "/" + indexName + "/_delete_by_query");
            request.addParameter("conflicts", "proceed");
            request.setJsonEntity("{\"query\": {\"match_all\": {}}}");

            Response response = opensearchClient.getLowLevelClient().performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200 && statusCode != 201) {
                String errorMessage = EntityUtils.toString(response.getEntity());
                log.error("Failed to delete all documents: " + errorMessage);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting documents: " + errorMessage);
            }

            log.info("Successfully deleted all documents in the '" + indexName + "' index.");
        } catch (IOException e) {
            log.error("Exception occurred while deleting all documents in the index: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete documents due to an IO exception", e);
        }
    }
}