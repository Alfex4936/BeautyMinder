package app.beautyminder.service.cosmetic;

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
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewSearchService {

    private final ReviewRepository reviewRepository;
    private final EsReviewRepository esReviewRepository;
    private final RestHighLevelClient opensearchClient;
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    //    @PostConstruct
    @Scheduled(cron = "0 0 2 * * ?") // everyday 2am
    public void indexReviews() {
        List<Review> reviews = reviewRepository.findAll();  // Fetch all reviews from MongoDB
        List<EsReview> esReviews = reviews.stream()
                .map(this::convertToEsReview)
                .collect(Collectors.toList());  // Convert to EsReview objects
        esReviewRepository.saveAll(esReviews);  // Index all reviews to Elasticsearch
        log.info(">>> Indexed all the reviews!!!");
    }

    private EsReview convertToEsReview(Review review) {
        // Conversion logic
        String cleanedContent = review.getContent().replace("\n", ".").trim();

        return EsReview.builder()
                .id(review.getId())
                .content(cleanedContent)
                .rating(review.getRating())
                .userName(review.getUser().getEmail())
                .cosmeticName(review.getCosmetic().getName())
                .build();
    }

    public String getIndexOfReviews() throws IOException {
        Request request = new Request("GET", "/reviews");
        Response response = opensearchClient.getLowLevelClient().performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    public String viewReviewsData() throws IOException {
        Request request = new Request("GET", "/reviews/_search");
        Response response = opensearchClient.getLowLevelClient().performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    public String analyzeText(String text) throws IOException {
        Request request = new Request("POST", "/reviews/_analyze");
        request.setJsonEntity("{\"text\": \"" + text + "\"}");
        Response response = opensearchClient.getLowLevelClient().performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }


    public void deleteAllIndices() {
        try {
            Request request = new Request("DELETE", "/reviews");
            Response response = opensearchClient.getLowLevelClient().performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                log.error("Failed to delete all indices: " + EntityUtils.toString(response.getEntity()));
            } else {
                log.info("Successfully deleted all indices in the 'reviews' index.");
            }
        } catch (IOException e) {
            log.error("Exception occurred while deleting all indices: ", e);
        }
    }

    public List<EsReview> searchByContent(String content) {
        FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery("content", content);
        SearchRequest searchRequest = new SearchRequest("reviews");
        searchRequest.source().query(fuzzyQueryBuilder);

        try {
            SearchResponse searchResponse = opensearchClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            List<EsReview> esReviews = new ArrayList<>();
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                EsReview esReview = convertJsonToEsReview(sourceAsString);
                esReviews.add(esReview);
            }
            return esReviews;
        } catch (IOException e) {
            log.error("Exception occurred while searching reviews: ", e);
            return new ArrayList<>();
        }
    }

    private EsReview convertJsonToEsReview(String json) {
        try {
            return objectMapper.readValue(json, EsReview.class);
        } catch (IOException e) {
            log.error("Failed to deserialize JSON to EsReview: ", e);
            return null;
        }
    }

}