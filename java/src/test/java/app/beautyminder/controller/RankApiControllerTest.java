package app.beautyminder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class RankApiControllerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, Integer> keywordSearchCount = new HashMap<>();
    private static final Map<String, Integer> keywordSearchCount2 = new HashMap<>();

    static {
        keywordSearchCount.put("스킨케어", 1000);  // Skincare
        keywordSearchCount.put("파운데이션", 15); // Foundation
        keywordSearchCount.put("쿠션", 200);      // Cushion
        keywordSearchCount.put("립스틱", 100);    // Lipstick
        keywordSearchCount.put("아이크림", 95);   // Eye cream
        keywordSearchCount.put("선크림", 400);      // Sunscreen
        keywordSearchCount.put("마스카라", 85);   // Mascara
        keywordSearchCount.put("클렌징오일", 80); // Cleansing oil
        keywordSearchCount.put("토너", 75);       // Toner
        keywordSearchCount.put("세럼", 70);       // Serum
        keywordSearchCount.put("에센스", 65);     // Essence
    }

    static {
        keywordSearchCount2.put("스킨케어", 15);  // Skincare
        keywordSearchCount2.put("파운데이션", 900); // Foundation
        keywordSearchCount2.put("쿠션", 10);      // Cushion
    }

//    @Autowired
//    private MockMvc mockMvc;

    @Test
    public void testSingleUpdate() {
        RunningStats stats = new RunningStats();
        stats.updateRecentCount(300);
        assertEquals(300, stats.getRecentCount());
        assertEquals(1, stats.getTotalCount());
        assertEquals(300.0, stats.getMean(), 0.001);
    }

    @Test
    public void testSignificantDeviation() {
        RunningStats stats = new RunningStats();
        assertEquals(stats.getTotalCount(), 0);

        stats.updateRecentCount(100);
        assertEquals(stats.getMean(), 100.0, 0.001);
        assertEquals(stats.getTotalCount(), 1);

        stats.updateRecentCount(105);
        assertEquals(stats.getMean(), (100 + 105) / 2.0, 0.001);
        assertFalse(stats.isSignificantDeviation(1.0));

        // Test case with a larger sample size to provide a meaningful standard deviation
        for (int i = 0; i < 1000; i++) {
            stats.updateRecentCount(100);
        }

        stats.updateRecentCount(150); // This should be a significant deviation
        assertTrue(stats.isSignificantDeviation(4.0));
    }

    private List<String> deserializeKeywords(String response) {
        // Deserialize the JSON response into a List of keywords
        try {
            return Arrays.asList(objectMapper.readValue(response, String[].class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize response", e);
        }
    }

    @ToString
    static class RunningStats {
        // Time decay factor
        private static final double DECAY_FACTOR = 0.95;
        // The unit for time decay, e.g., if decay is per hour, then unit is HOUR_IN_MILLIS
        private static final long TIME_UNIT = 60000;
        private final AtomicLong recentCount = new AtomicLong();
        private final AtomicLong totalCount = new AtomicLong();
        private final AtomicReference<Double> mean = new AtomicReference<>(0.0);
        private final AtomicReference<Double> M2 = new AtomicReference<>(0.0);
        private final AtomicLong lastUpdated = new AtomicLong();
        private final AtomicReference<Double> lastValue = new AtomicReference<>(0.0);

        void updateRecentCount(long value) {
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - lastUpdated.getAndUpdate(x -> currentTime); // Update lastUpdated

            // Apply decay factor on every update since updates are frequent
            double decay = Math.pow(DECAY_FACTOR, timeDiff / (double) TIME_UNIT);
            recentCount.updateAndGet(x -> (long) (x * decay + value));

            long newTotalCount = totalCount.incrementAndGet();
            double oldMean = mean.get();

            // Calculate the new mean
            double newMean = oldMean + (value - oldMean) / newTotalCount;
            mean.set(newMean); // Set the new mean

            // Update M2 for variance calculation
            double delta = value - oldMean;
            double delta2 = value - newMean;
            M2.getAndUpdate(m -> m + delta * delta2);

            lastValue.set((double) value); // Update lastValue to the new value
        }

        boolean isSignificantDeviation(double significanceLevel) {
            if (totalCount.get() < 2) {
                return false; // Not enough data to determine deviation
            }

            double variance = M2.get() / (totalCount.get() - 1);
            double stddev = Math.sqrt(variance);

            // Use the lastValue to retrieve the last individual measurement
            double lastMeasurement = lastValue.get();

            // In isSignificantDeviation, use lastValue.get() to retrieve the last value
            double z = (lastMeasurement - mean.get()) / stddev;

            // Check if the absolute z-score is greater than the significance level
            return Math.abs(z) > significanceLevel;
        }

        // Getter for recentCount
        public long getRecentCount() {
            return recentCount.get();
        }

        public long getTotalCount() {
            return totalCount.get();
        }

        public Double getMean() {
            return mean.get();
        }
    }
}
