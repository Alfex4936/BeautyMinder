package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.EsReview;
import app.beautyminder.domain.Review;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.repository.elastic.EsReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewSearchService {

    private final ReviewRepository reviewRepository;
    private final EsReviewRepository esReviewRepository;
    private final RestHighLevelClient opensearchClient;

    //    @PostConstruct
    // @Scheduled(cron = "0 0 2 * * ?") // everyday 2am
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
        return EsReview.builder()
                .id(review.getId())
                .content(review.getContent())
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

    public void deleteAllIndices() {
        try {
            Request request = new Request("DELETE", "/reviews");
            Response response = opensearchClient.getLowLevelClient().performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                log.error("Failed to delete all indices: " + EntityUtils.toString(response.getEntity()));
            } else {
                log.info("Successfully deleted all indices in the 'cosmetics' index.");
            }
        } catch (IOException e) {
            log.error("Exception occurred while deleting all indices: ", e);
        }
    }

    public List<EsReview> searchByContent(String content) {
        return esReviewRepository.findByContentContaining(content);
    }
}