package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.EsReview;
import app.beautyminder.domain.Review;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.repository.elastic.EsReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.Request;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Response;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewSearchService {

    private final ReviewRepository reviewRepository;
    private final EsReviewRepository esReviewRepository;
    private final RestHighLevelClient opensearchClient;
    private final ObjectMapper objectMapper;

    //    @PostConstruct
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Seoul") // everyday 2am
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
        var request = new Request("POST", "/reviews/_analyze");
        String jsonEntity = """
                {
                  "text": "%s"
                }
                """.formatted(text);
        request.setJsonEntity(jsonEntity);

        Response response = opensearchClient.getLowLevelClient().performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }


    public void delete() {
        var request = new Request("DELETE", "/reviews");
        try {
            var response = opensearchClient.getLowLevelClient().performRequest(request);
            var statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                log.error("Failed to delete all indices: " + EntityUtils.toString(response.getEntity()));
            } else {
                log.info("Successfully deleted all indices in the 'reviews' index.");
            }
        } catch (IOException e) {
            log.error("Exception occurred while deleting all indices: ", e);
        }
    }

    public List<Review> searchByContent(String content) {
        var fuzzyQueryBuilder = QueryBuilders.fuzzyQuery("content", content);
        var searchRequest = new SearchRequest("reviews");
        searchRequest.source().query(fuzzyQueryBuilder);

        try {
            var searchResponse = opensearchClient.search(searchRequest, RequestOptions.DEFAULT);
            var hits = searchResponse.getHits();

            return Stream.of(hits.getHits())
                    .map(SearchHit::getSourceAsString)
                    .map(this::convertJsonToEsReview).filter(Objects::nonNull)
                    .map(EsReview::getId)
                    .map(reviewRepository::findById)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Exception occurred while searching reviews: ", e);
            return Collections.emptyList();
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