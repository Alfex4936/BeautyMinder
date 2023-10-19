package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.dto.CosmeticMetricData;
import app.beautyminder.repository.CosmeticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CosmeticMetricService {

    private final CosmeticRepository cosmeticRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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

    @Scheduled(cron = "0 0 7 * * ?") // everyday 7am
    public void resetCounts() {
        Set<String> keys = redisTemplate.keys("cosmeticMetrics:*");
        if (keys != null) {
            for (String key : keys) {
                redisTemplate.delete(key);
            }
        }
        log.info("REDIS: Reset all cosmetic metrics.");
    }

    private String buildRedisKey(String cosmeticId) {
        return String.format(COSMETIC_METRICS_KEY_TEMPLATE, cosmeticId);
    }

    public void incrementMetric(String cosmeticId, String metric) {
        String key = buildRedisKey(cosmeticId);
        redisTemplate.opsForHash().increment(key, metric, 1);
    }

    public void incrementClickCount(String cosmeticId) {
        incrementMetric(cosmeticId, "clicks");
    }

    public void incrementHitCount(String cosmeticId) {
        incrementMetric(cosmeticId, "hits");
    }

    public void incrementFavCount(String cosmeticId) {
        incrementMetric(cosmeticId, "favs");
    }

    public void decreaseFavCount(String cosmeticId) {
        String key = buildRedisKey(cosmeticId);
        redisTemplate.opsForHash().increment(key, "favs", -1);
    }

    public List<Cosmetic> getTopRankedCosmetics(int size) {
        // Retrieve all keys for the cosmetics metrics
        Set<String> keys = redisTemplate.keys("cosmeticMetrics:*");

        // This map will hold the calculated scores for each cosmetic ID
        Map<String, Double> cosmeticScores = new HashMap<>();

        if (keys != null) {
            for (String key : keys) {
                Map<Object, Object> metrics = redisTemplate.opsForHash().entries(key);

                // Retrieve each metric and handle potential null values
                double clicks = Double.parseDouble((String) metrics.getOrDefault("clicks", "0"));
                double hits = Double.parseDouble((String) metrics.getOrDefault("hits", "0"));
                double favs = Double.parseDouble((String) metrics.getOrDefault("favs", "0"));

                // Calculate the score based on the retrieved metrics
                double score = clicks * CLICK_WEIGHT + hits * HIT_WEIGHT + favs * FAV_WEIGHT;

                // Extract the cosmeticId from the key (assuming the key is in the format "cosmeticMetrics:{cosmeticId}")
                String cosmeticId = key.split(":")[1];

                // Add the calculated score to the map
                cosmeticScores.put(cosmeticId, score);
            }
        }

        // Sort the entries in the map by score in descending order and limit the number of results to 'size'
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(cosmeticScores.entrySet());
        sortedEntries.sort(Map.Entry.<String, Double>comparingByValue().reversed());

        // Collect the top cosmetic IDs based on the sorted scores
        List<String> topCosmeticIds = sortedEntries.stream()
                .limit(size)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Fetch Cosmetic objects from the repository using the collected top cosmetic IDs
        // The repository method used here should be capable of handling a list of IDs for fetching multiple records
        return cosmeticRepository.findAllById(topCosmeticIds);
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
                data.setClickCount(Long.parseLong((String) metrics.getOrDefault("clicks", "0")));
                data.setHitCount(Long.parseLong((String) metrics.getOrDefault("hits", "0")));
                data.setFavCount(Long.parseLong((String) metrics.getOrDefault("favs", "0")));

                allCounts.add(data);
            }
        }
        return allCounts;
    }
}