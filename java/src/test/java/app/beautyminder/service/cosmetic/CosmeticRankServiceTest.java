package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Review;
import app.beautyminder.dto.CosmeticMetricData;
import app.beautyminder.dto.Event;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.KeywordRankRepository;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.service.MongoService;
import app.beautyminder.service.review.NlpService;
import app.beautyminder.util.EventQueue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vane.badwordfiltering.BadWordFiltering;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CosmeticRankServiceTest {

    @Mock
    private CosmeticRepository cosmeticRepository;
    @Mock
    private KeywordRankRepository keywordRankRepository;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;
    @Mock
    private EventQueue eventQueue;
    @Mock
    private MongoService mongoService;
    @InjectMocks
    private CosmeticRankService cosmeticRankService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set up the processQueue field using reflection it's private
        ReflectionTestUtils.setField(cosmeticRankService, "keywordStatsMap", new HashMap<>());
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    public void testProcessEvents() {
        // Setup
        Event clickEvent = new Event("cosmeticId1", CosmeticRankService.ActionType.CLICK);
        Event hitEvent = new Event("cosmeticId2", CosmeticRankService.ActionType.HIT);
        Event favEvent = new Event("cosmeticId3", CosmeticRankService.ActionType.FAV);

        when(eventQueue.dequeueAll()).thenReturn(List.of(clickEvent, hitEvent, favEvent));
        when(hashOperations.entries(any())).thenReturn(new HashMap<>()); // Mocking Redis HashOperations entries
        // Execute
        cosmeticRankService.processEvents();

        // Verify
        verify(eventQueue).dequeueAll();
    }

    @Test
    public void testGetAllCosmeticCounts() {
        // Setup
        Set<String> mockKeys = Set.of("cosmeticMetrics:cosmeticId1", "cosmeticMetrics:cosmeticId2");
        when(redisTemplate.keys("cosmeticMetrics:*")).thenReturn(mockKeys);

        Map<Object, Object> mockMetrics = new HashMap<>();
        mockMetrics.put(CosmeticRankService.ActionType.CLICK.getActionString(), "5");
        mockMetrics.put(CosmeticRankService.ActionType.HIT.getActionString(), "10");
        mockMetrics.put(CosmeticRankService.ActionType.FAV.getActionString(), "3");

        when(hashOperations.entries(anyString())).thenReturn(mockMetrics);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        // Execute
        List<CosmeticMetricData> result = cosmeticRankService.getAllCosmeticCounts();

        // Verify
        assertEquals(2, result.size()); // Asserting the size of the result list
    }
}