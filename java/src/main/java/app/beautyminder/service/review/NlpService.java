package app.beautyminder.service.review;

import app.beautyminder.domain.Review;
import app.beautyminder.dto.PyReviewAnalysis;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.service.MongoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vane.badwordfiltering.BadWordFiltering;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NlpService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ReviewRepository reviewRepository;
    private final MongoService mongoService;
    private final BadWordFiltering badWordFiltering;

    @Value("${server.python.review}")
    private String reviewProcessServer;
    private final Set<String> processQueue = new HashSet<>();

    // every 10min it checks failed reviews and re-call analysis server.
    @Scheduled(cron = "0 0/10 * * * ?", zone = "Asia/Seoul")
    public void processFailedReviews() {
        if (processQueue.isEmpty()) {
            return;
        }

        Set<String> toRemove = new HashSet<>();

        processQueue.forEach(reviewId -> {
            reviewRepository.findById(reviewId).ifPresentOrElse(
                    review -> {
                        try {
                            var reviewJson = objectMapper.writeValueAsString(review);
                            var nlpResultJson = callProcessAPI(reviewJson).getBody();

                            var isFiltered = badWordFiltering.check(review.getContent());
                            var nlpAnalysis = nlpResultJson.nlpAnalysis();

                            mongoService.updateFields(review.getId(),
                                    Map.of("isFiltered", isFiltered, "nlpAnalysis", nlpAnalysis), Review.class);

                            toRemove.add(reviewId);
                        } catch (JsonProcessingException | RestClientException e) {
                            log.error("Failed to process review asynchronously: " + reviewId, e);
                        }
                    },
                    () -> log.warn("Review not found for ID: " + reviewId)
            );
        });

        processQueue.removeAll(toRemove);
    }

    @Async
    public void processReviewAsync(Review review) {
        try {
            processQueue.add(review.getId());
            var reviewJson = objectMapper.writeValueAsString(review);
            var nlpResultJson = callProcessAPI(reviewJson).getBody();

            var isFiltered = badWordFiltering.check(review.getContent());
            var nlpAnalysis = nlpResultJson.nlpAnalysis();

            mongoService.updateFields(review.getId(),
                    Map.of("isFiltered", isFiltered, "nlpAnalysis", nlpAnalysis), Review.class);

//            reviewRepository.save(review);
            processQueue.remove(review.getId());
            log.info("BEMINDER: async-ly updated Review's NLP analysis.");
        } catch (JsonProcessingException | RestClientException e) {
            log.error("Failed to process review asynchronously", e);
        }
    }

    private ResponseEntity<PyReviewAnalysis> callProcessAPI(String reviewJson) throws RestClientException {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request entity
        var request = new HttpEntity<>(reviewJson, headers);

        // Send POST request and get JSON
        return restTemplate.postForEntity(reviewProcessServer, request, PyReviewAnalysis.class);

//        log.info("BEMINDER: python server: {}", response.getBody().toString());
    }
}
