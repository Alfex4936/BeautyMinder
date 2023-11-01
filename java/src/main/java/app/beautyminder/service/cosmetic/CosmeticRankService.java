package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.KeywordRank;
import app.beautyminder.dto.CosmeticMetricData;
import app.beautyminder.dto.Event;
import app.beautyminder.dto.KeywordEvent;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.KeywordRankRepository;
import app.beautyminder.util.EventQueue;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CosmeticRankService {

    // Redis structure: "cosmeticMetrics:{cosmeticId}" -> {"clicks": "10", "hits": "5", "favs": "3"}
    private static final String COSMETIC_METRICS_KEY_TEMPLATE = "cosmeticMetrics:%s";
    private static final Integer HIGH_VOLUME_THRESHOLD = 100; // imagine average searches is around 100
    private static final Integer LOW_VOLUME_THRESHOLD = 10; // imagine low searches is around 10
    private static final double HIGH_SIG_LEVEL = 3.0;
    private static final double LOW_SIG_LEVEL = 2.0;
    private final CosmeticRepository cosmeticRepository;
    private final KeywordRankRepository keywordRankRepository;
    private final EventQueue eventQueue;
    private final String KEYWORD_METRICS_KEY = "keywordMetrics";
    private final Map<String, RunningStats> keywordStatsMap = new HashMap<>();
    /*

    현재 실시간 수집 중인 정보:
        1. 제품 클릭
        2. 제품 검색 hit (검색 했을 때 제품이 노출된 횟수)
        3. 매일 즐겨찾기에 추가된 수

     */
    private final double CLICK_WEIGHT = 1.8;
    // clicks might be considered more valuable as they represent a user taking a clear action based on their interest
    private final double HIT_WEIGHT = 1.1;
    private final double FAV_WEIGHT = 2.0;
    @Getter
    private String cronExpression = "*/35 * * * * ?"; // every 35 seconds

    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    @Scheduled(cron = "0 0 4 * * ?") // everyday 4am
    public void resetCounts() {
        Set<String> keys = redisTemplate.keys("cosmeticMetrics:*");

        if (keys != null) {
            for (String key : keys) {
                redisTemplate.delete(key);
            }
        }

        redisTemplate.delete(KEYWORD_METRICS_KEY);
        keywordStatsMap.clear();

        log.info("REDIS: Reset all cosmetic metrics.");
    }

    // Cosmetic Metrics collection (click/search hit)
    // Scheduled Batch Processing
    @Scheduled(cron = "0 0/10 * * * ?") // Every 10 minutes
    @Transactional
    public void processEvents() {
        List<Event> events = eventQueue.dequeueAll();
        if (events.isEmpty()) {
            log.info("Metric Redis batch empty");
            return;
        }
        Map<String, CosmeticMetricData> aggregatedData = new HashMap<>();

        for (Event event : events) {
            aggregatedData.merge(event.getCosmeticId(), new CosmeticMetricData(), (existingData, newData) -> {
                existingData.setCosmeticId(event.getCosmeticId());

                switch (event.getType()) {
                    case CLICK:
                        existingData.setClickCount((existingData.getClickCount() != null ? existingData.getClickCount() : 0) + 1);
                        break;
                    case HIT:
                        existingData.setHitCount((existingData.getHitCount() != null ? existingData.getHitCount() : 0) + 1);
                        break;
                    case FAV:
                        existingData.setFavCount((existingData.getFavCount() != null ? existingData.getFavCount() : 0) + 1);
                        break;
                }

                return existingData;
            });
        }

        for (Map.Entry<String, CosmeticMetricData> entry : aggregatedData.entrySet()) {
            String cosmeticId = entry.getKey();
            CosmeticMetricData data = entry.getValue();
            updateRedisMetrics(cosmeticId, data);
        }

        log.info("REDIS: Sending batches");
    }

    // -------- Keyword collection
    @Bean
    public ScheduledTaskRegistrar scheduledTaskRegistrar() {
        ScheduledTaskRegistrar registrar = new ScheduledTaskRegistrar();
        registrar.addTriggerTask(this::processKeywordEvents, triggerContext -> {
            CronTrigger trigger = new CronTrigger(getCronExpression());
            return trigger.nextExecution(triggerContext);
        });

        return registrar;
    }

    public void updateCronExpressionBasedOnVolume(long dataVolume) {
        // Here, set a new cron expression based on the data volume
        if (dataVolume < LOW_VOLUME_THRESHOLD) {
            setCronExpression("*/60 * * * * ?");  // Set to every 60 seconds if volume is low
        } else {
            setCronExpression("*/35 * * * * ?");  // Set back to every 35 seconds if volume is normal/high
        }
    }

    public void setCronExpression(String newExpression) {
        cronExpression = newExpression;
    }

    public double calculateSignificanceLevel(long dataVolume) {
        // Increase significance level during high data volume periods
        if (dataVolume > HIGH_VOLUME_THRESHOLD) {
            return HIGH_SIG_LEVEL;
        } else {
            return LOW_SIG_LEVEL;
        }
    }

    @Transactional
    public void processKeywordEvents() {
        List<KeywordEvent> events = eventQueue.dequeueAllKeywords();
        if (events.isEmpty()) {
            log.info("Keyword Redis batch empty");
            updateCronExpressionBasedOnVolume(0);
            return;
        }

        long dataVolume = events.size();  // number of events
        double adaptiveSigLevel = calculateSignificanceLevel(dataVolume);

        Map<String, Long> keywordCountMap = new HashMap<>();

        for (KeywordEvent event : events) {
            keywordCountMap.merge(event.getKeyword(), 1L, Long::sum);
        }

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            RedisHashCommands hashCommands = connection.hashCommands();

            for (Map.Entry<String, Long> entry : keywordCountMap.entrySet()) {
                String keyword = entry.getKey();
                Long count = entry.getValue();

                // Update running statistics for each keyword
                RunningStats stats = keywordStatsMap.computeIfAbsent(keyword, k -> new RunningStats());
                stats.updateRecentCount(count);  // Update the recent count, not the total count

                /*
                통계적 유의성
                    Increasing the significanceLevel (e.g., to 3.0)
                        would make the system less sensitive to deviations, requiring a larger change in search frequency to consider a keyword as trending.
                        This might result in fewer keywords being identified as trending but with higher confidence that the change is significant.
                    Decreasing the significanceLevel (e.g., to 1.0)
                        would make the system more sensitive to deviations, potentially identifying more keywords as trending.
                        However, this might also increase the likelihood of identifying noise or less meaningful trends.
                 */
                // Default to incrementing on the first run, or check for significant deviation in subsequent runs
                boolean shouldIncrement = stats.isSignificantDeviation(adaptiveSigLevel);

                if (shouldIncrement) {
                    log.info("Incrementing Redis count for keyword: {} by {}", keyword, count);
                    hashCommands.hIncrBy(KEYWORD_METRICS_KEY.getBytes(), keyword.getBytes(), count);
                }
            }
            return null; // Return value can be ignored when using executePipelined
        });

        log.info("REDIS: Processed keyword batches ({})", dataVolume);

        updateCronExpressionBasedOnVolume(dataVolume);
    }

    // Scheduled Batch Processing to log top 10 keywords every 15mins
    @Scheduled(cron = "0 0/5 * * * ?") // every 15min
    public void saveTop10Keywords() {
        // Get all keyword counts from Redis
        Map<Object, Object> keywordCounts = redisTemplate.opsForHash().entries(KEYWORD_METRICS_KEY);

        // Sort keywords by counts in descending order
        List<Map.Entry<Object, Object>> sortedEntries = keywordCounts.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(
                        Long.parseLong((String) e2.getValue()),
                        Long.parseLong((String) e1.getValue())
                ))
                .limit(10)
                .toList();

        // Log the top 10 keywords along with their counts
        for (Map.Entry<Object, Object> entry : sortedEntries) {
            log.info("Keyword: {}, Count: {}", entry.getKey(), entry.getValue());
        }

        // Extract the top 10 keywords
        List<String> top10Keywords = sortedEntries.stream()
                .map(entry -> (String) entry.getKey())
                .toList();

        if (top10Keywords.isEmpty()) {
            log.info("Empty top 10 keywords");
            return;
        }

        log.info("Saving top 10 keywords: {}", top10Keywords);

        // Create a new KeywordRank instance
        KeywordRank keywordRank = KeywordRank.builder()
                .rankings(top10Keywords)
                .build();


        // Save the KeywordRank instance to the database
        keywordRankRepository.save(keywordRank);
    }

    // Methods to collect events in the intermediate data store
    public void collectEvent(String cosmeticId, ActionType type) {
        eventQueue.enqueue(new Event(cosmeticId, type));
    }

    // Collects keyword search events
    public void collectSearchEvent(String keyword) {
        eventQueue.enqueueKeyword(new KeywordEvent(keyword.trim()));
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


    static class RunningStats {
        private final AtomicLong recentCount = new AtomicLong();
        private final AtomicLong totalCount = new AtomicLong();
        private final AtomicReference<Double> mean = new AtomicReference<>(0.0);
        private final AtomicReference<Double> M2 = new AtomicReference<>(0.0);

        void updateRecentCount(long value) {
            recentCount.set(value);  // Set the recent count
            totalCount.addAndGet(value);  // Increment the total count by the recent count value
            double delta = value - mean.get();
            mean.set(mean.get() + delta / totalCount.get());  // Use totalCount.get() instead of count
            M2.set(M2.get() + delta * (value - mean.get()));
        }

        boolean isSignificantDeviation(double significanceLevel) {
            if (totalCount.get() < 2) {
                return false;  // Not enough data to determine deviation
            }

            double variance = M2.get() / (totalCount.get() - 1);
            double stddev = Math.sqrt(variance);

            // Calculate the z-score
            double z = (recentCount.get() - mean.get()) / stddev;

            // Check if the absolute z-score is greater than the significance level
            return Math.abs(z) > significanceLevel;
        }
    }
}