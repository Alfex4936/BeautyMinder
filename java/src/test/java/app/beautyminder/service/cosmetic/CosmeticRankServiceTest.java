package app.beautyminder.service.cosmetic;

import app.beautyminder.dto.CosmeticMetricData;
import app.beautyminder.dto.Event;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.KeywordRankRepository;
import app.beautyminder.service.MongoService;
import app.beautyminder.util.EventQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
        Event clickEvent = new Event("652cdc2d2bf53d0109d1e210", CosmeticRankService.ActionType.CLICK);
        Event hitEvent = new Event("652cdc2d2bf53d0109d1e211", CosmeticRankService.ActionType.HIT);
        Event favEvent = new Event("652cdc2d2bf53d0109d1e212", CosmeticRankService.ActionType.FAV);

        cosmeticRankService.collectClickEvent("652cdc2d2bf53d0109d1e210");
        cosmeticRankService.collectHitEvent("652cdc2d2bf53d0109d1e211");
        cosmeticRankService.collectFavEvent("652cdc2d2bf53d0109d1e212");

        when(eventQueue.dequeueAll()).thenReturn(List.of(clickEvent, hitEvent, favEvent));
        when(hashOperations.entries(any())).thenReturn(new HashMap<>()); // Mocking Redis HashOperations entries
        // Execute
        cosmeticRankService.processEvents();

        // Verify
        verify(eventQueue).dequeueAll();
        verify(redisTemplate, times(6)).opsForHash();
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