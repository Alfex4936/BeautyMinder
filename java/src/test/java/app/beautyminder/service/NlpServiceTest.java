package app.beautyminder.service;

import app.beautyminder.domain.Review;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.service.review.NlpService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vane.badwordfiltering.BadWordFiltering;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class NlpServiceTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private MongoService mongoService;
    @Mock
    private BadWordFiltering badWordFiltering;

    @InjectMocks
    private NlpService nlpService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set up the processQueue field using reflection it's private
        ReflectionTestUtils.setField(nlpService, "processQueue", new HashSet<>());
    }

    @Test
    public void testProcessFailedReviews() throws JsonProcessingException {
        Set<String> processQueue = new HashSet<>();
        processQueue.add("reviewId1");
        processQueue.add("reviewId2");
        ReflectionTestUtils.setField(nlpService, "processQueue", processQueue);

        // Setup
        var review = Review.builder().build();
        when(reviewRepository.findById(anyString())).thenReturn(Optional.of(review));
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Test exception") {
        });

        // Execute
        nlpService.processFailedReviews();

        // Verify
        verify(mongoService, never()).updateFields(anyString(), anyMap(), any());
    }

    @Test
    public void testProcessFailedReviews_ExceptionThrown() throws JsonProcessingException {
        // Setup for exception scenario
        var review = Review.builder().build();
        when(reviewRepository.findById(anyString())).thenReturn(Optional.of(review));
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Test exception") {
        });

        // Execute
        nlpService.processFailedReviews();

        // Verify that no updateFields call is made to mongoService
        verify(mongoService, never()).updateFields(anyString(), anyMap(), any());
    }

    @Test
    public void testProcessFailedReviews_ReviewNotFound() {
        // Setup for not found scenario
        when(reviewRepository.findById(anyString())).thenReturn(Optional.empty());

        // Execute
        nlpService.processFailedReviews();
    }
}