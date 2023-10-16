package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.repository.CosmeticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CosmeticMetricService {

    private final CosmeticRepository cosmeticRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
        redisTemplate.opsForHash().delete("clickCounts");
        redisTemplate.opsForHash().delete("hitCounts");
        redisTemplate.opsForHash().delete("favCounts");
        redisTemplate.delete("cosmetic_scores");
        log.info("REDIS: Reset all click, hit counts, and scores.");
    }

    public void incrementClickCount(String cosmeticId) { // upsert
        redisTemplate.opsForHash().increment("clickCounts", cosmeticId, 1);
    }

    public void incrementHitCount(String cosmeticId) {
        redisTemplate.opsForHash().increment("hitCounts", cosmeticId, 1);
    }

    public void incrementFavCount(String cosmeticId) {
        redisTemplate.opsForHash().increment("favCounts", cosmeticId, 1);
    }

    public void decreaseFavCount(String cosmeticId) {
        redisTemplate.opsForHash().increment("favCounts", cosmeticId, -1);
    }

    public List<Cosmetic> getTopRankedCosmetics(int size) {
        // Fetch
        Map<Object, Object> clickCounts = redisTemplate.opsForHash().entries("clickCounts");
        Map<Object, Object> hitCounts = redisTemplate.opsForHash().entries("hitCounts");
        Map<Object, Object> favCounts = redisTemplate.opsForHash().entries("favCounts");

        // Use Redis pipelining to update scores in a batch
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Object cosmeticId : clickCounts.keySet()) {
                String clickCountStr = (String) clickCounts.get(cosmeticId);
                double clickCount = clickCountStr != null ? Double.parseDouble(clickCountStr) : 0;

                String hitCountStr = (String) hitCounts.get(cosmeticId);
                double hitCount = hitCountStr != null ? Double.parseDouble(hitCountStr) : 0;

                String favCountStr = (String) favCounts.get(cosmeticId);
                double favCount = hitCountStr != null ? Double.parseDouble(favCountStr) : 0;

                double score = clickCount * CLICK_WEIGHT + hitCount * HIT_WEIGHT + favCount * FAV_WEIGHT;
                connection.zAdd("cosmetic_scores".getBytes(), score, ((String) cosmeticId).getBytes());
            }
            return null;
        });

        // Fetch top ranked cosmetics from the Sorted Set
        Set<Object> topCosmeticIds = redisTemplate.opsForZSet().reverseRange("cosmetic_scores", 0, size - 1);

        // Check if topCosmeticIds is null before proceeding
        if (topCosmeticIds == null) {
            log.warn("No top cosmetic IDs found.");
            return Collections.emptyList();
        }

        // Fetch Cosmetic objects from the repository
        return topCosmeticIds.stream()
                .map(cosmeticId -> cosmeticRepository.findById((String) cosmeticId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}