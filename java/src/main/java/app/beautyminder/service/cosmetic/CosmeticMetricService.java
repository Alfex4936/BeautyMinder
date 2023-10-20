package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.dto.CosmeticMetricData;
import app.beautyminder.dto.Event;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.service.EventQueue;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CosmeticMetricService {

    private final CosmeticRepository cosmeticRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final EventQueue eventQueue;

    // Redis structure: "cosmeticMetrics:{cosmeticId}" -> {"clicks": "10", "hits": "5", "favs": "3"}
    private static final String COSMETIC_METRICS_KEY_TEMPLATE = "cosmeticMetrics:%s";


    // clicks might be considered more valuable as they represent a user taking a clear action based on their interest

    /*
   
    현재 실시간 수집 중인 정보:
        1. 제품 클릭
        2. 제품 검색 hit (검색 했을 때 제품이 노출된 횟수)
        3. 매일 즐겨찾기에 추가된 수
    
     */
    private final double CLICK_WEIGHT = 1.8;
    private final double HIT_WEIGHT = 1.1;
    private final double FAV_WEIGHT = 2.0;


    @PostConstruct
    @Scheduled(cron = "0 0 4 * * ?") // everyday 4am
    public void resetCounts() {
        Set<String> keys = redisTemplate.keys("cosmeticMetrics:*");
        if (keys != null) {
            for (String key : keys) {
                redisTemplate.delete(key);
            }
        }
        log.info("REDIS: Reset all cosmetic metrics.");
    }

    // Scheduled Batch Processing
    @Scheduled(cron = "0 0/10 * * * ?") // Every 10 minutes
    @Transactional
    public void processEvents() {
        List<Event> events = eventQueue.dequeueAll();
        Map<String, CosmeticMetricData> aggregatedData = new HashMap<>();

        for (Event event : events) {
            CosmeticMetricData data = aggregatedData.getOrDefault(event.getCosmeticId(), new CosmeticMetricData());
            data.setCosmeticId(event.getCosmeticId());

            switch (event.getType()) {
                case CLICK:
                    data.setClickCount((data.getClickCount() != null ? data.getClickCount() : 0) + 1);
                    break;
                case HIT:
                    data.setHitCount((data.getHitCount() != null ? data.getHitCount() : 0) + 1);
                    break;
                case FAV:
                    data.setFavCount((data.getFavCount() != null ? data.getFavCount() : 0) + 1);
                    break;
            }
            aggregatedData.put(event.getCosmeticId(), data);
        }

        for (Map.Entry<String, CosmeticMetricData> entry : aggregatedData.entrySet()) {
            String cosmeticId = entry.getKey();
            CosmeticMetricData data = entry.getValue();
            updateRedisMetrics(cosmeticId, data);
        }

        log.info("REDIS: Sending batches");
    }

    // Methods to collect events in the intermediate data store
    public void collectEvent(String cosmeticId, ActionType type) {
        eventQueue.enqueue(new Event(cosmeticId, type));
    }

    public void collectClickEvent(String cosmeticId) {
        collectEvent(cosmeticId, ActionType.CLICK);
    }

    public void collectHitEvent(String cosmeticId) {
        collectEvent(cosmeticId, ActionType.HIT);
    }

    public void collectFavEvent(String cosmeticId) {
        collectEvent(cosmeticId, ActionType.FAV);
    }

    private void updateRedisMetrics(String cosmeticId, CosmeticMetricData data) {
        String key = buildRedisKey(cosmeticId);
        // Retrieve existing counts from Redis
        Map<Object, Object> existingCounts = redisTemplate.opsForHash().entries(key);

        long existingClickCount = existingCounts.containsKey(ActionType.CLICK.getActionString())
                ? Long.parseLong((String) existingCounts.get(ActionType.CLICK.getActionString())) : 0;
        long existingHitCount = existingCounts.containsKey(ActionType.HIT.getActionString())
                ? Long.parseLong((String) existingCounts.get(ActionType.HIT.getActionString())) : 0;
        long existingFavCount = existingCounts.containsKey(ActionType.FAV.getActionString())
                ? Long.parseLong((String) existingCounts.get(ActionType.FAV.getActionString())) : 0;

        // Add new counts to existing counts
        long newClickCount = (data.getClickCount() != null ? data.getClickCount() : 0) + existingClickCount;
        long newHitCount = (data.getHitCount() != null ? data.getHitCount() : 0) + existingHitCount;
        long newFavCount = (data.getFavCount() != null ? data.getFavCount() : 0) + existingFavCount;

        // Update Redis with new total counts
        redisTemplate.opsForHash().putAll(key, Map.of(
                ActionType.CLICK.getActionString(), Long.toString(newClickCount),
                ActionType.HIT.getActionString(), Long.toString(newHitCount),
                ActionType.FAV.getActionString(), Long.toString(newFavCount)
        ));
    }

    private String buildRedisKey(String cosmeticId) {
        return String.format(COSMETIC_METRICS_KEY_TEMPLATE, cosmeticId);
    }


    public List<Cosmetic> getTopRankedCosmetics(int size) {
        // Retrieve all keys for the cosmetics metrics
        Set<String> keys = redisTemplate.keys("cosmeticMetrics:*");
        if (keys == null) {
            return Collections.emptyList();  // handle null keys gracefully
        }

        // This map will hold the calculated scores for each cosmetic ID
        Map<String, Double> cosmeticScores = new HashMap<>();

        for (String key : keys) {
            Map<Object, Object> metrics = redisTemplate.opsForHash().entries(key);

            // Retrieve each metric and handle potential null values
            // Convert string values from Redis to Long
            long clicks = Long.parseLong((String) metrics.getOrDefault(ActionType.CLICK.getActionString(), "0"));
            long hits = Long.parseLong((String) metrics.getOrDefault(ActionType.HIT.getActionString(), "0"));
            long favs = Long.parseLong((String) metrics.getOrDefault(ActionType.FAV.getActionString(), "0"));

            // Calculate the score based on the retrieved metrics
            double score = clicks * CLICK_WEIGHT + hits * HIT_WEIGHT + favs * FAV_WEIGHT;

            // Extract the cosmeticId from the key (assuming the key is in the format "cosmeticMetrics:{cosmeticId}")
            String cosmeticId = key.split(":")[1];

            // Add the calculated score to the map
            cosmeticScores.put(cosmeticId, score);
        }

        // Sort the entries in the map by score in descending order
        Map<String, Double> sortedScores = cosmeticScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        // Limit the number of results to 'size'
        List<String> topCosmeticIds = sortedScores.keySet().stream()
                .limit(size)
                .toList();

        // Fetch Cosmetic objects from the repository using the collected top cosmetic IDs
        List<Cosmetic> cosmetics = cosmeticRepository.findAllById(topCosmeticIds);

        // Create a map of cosmetics keyed by their id for easy lookup
        Map<String, Cosmetic> cosmeticMap = cosmetics.stream()
                .collect(Collectors.toMap(Cosmetic::getId, Function.identity()));

        // Build an ordered list of cosmetics based on the order of topCosmeticIds
        return topCosmeticIds.stream()
                .map(cosmeticMap::get)
                .collect(Collectors.toList());
        // Fetch Cosmetic objects from the repository using the collected top cosmetic IDs
        // The repository method used here should be capable of handling a list of IDs for fetching multiple records
//        return cosmeticRepository.findAllById(topCosmeticIds); // findAllById doesn't guarantee the order
    }


    public List<CosmeticMetricData> getAllCosmeticCounts() {
        // You might need to adjust the key retrieval depending on how your keys are stored and how many there are.
        Set<String> keys = redisTemplate.keys("cosmeticMetrics:*");
        List<CosmeticMetricData> allCounts = new ArrayList<>();

        if (keys != null) {
            for (String fullKey : keys) {
                Map<Object, Object> metrics = redisTemplate.opsForHash().entries(fullKey);

                CosmeticMetricData data = new CosmeticMetricData();
                data.setCosmeticId(fullKey.replace("cosmeticMetrics:", "")); // Extract the cosmetic ID from the key

                // Set counts, ensuring we handle nulls gracefully by treating them as zeroes
                // Convert string values from Redis to Long
                data.setClickCount(Long.valueOf((String) metrics.getOrDefault(ActionType.CLICK.getActionString(), "0")));
                data.setHitCount(Long.valueOf((String) metrics.getOrDefault(ActionType.HIT.getActionString(), "0")));
                data.setFavCount(Long.valueOf((String) metrics.getOrDefault(ActionType.FAV.getActionString(), "0")));


                allCounts.add(data);
            }
        }
        return allCounts;
    }

    @Getter
    public enum ActionType {
        CLICK("clicks"),
        HIT("hits"),
        FAV("favs");

        private final String actionString;

        ActionType(String actionString) {
            this.actionString = actionString;
        }

    }

}