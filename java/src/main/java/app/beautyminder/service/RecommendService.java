package app.beautyminder.service;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import app.beautyminder.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendService {

    private final CosmeticRankService cosmeticRankService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final CosmeticRepository cosmeticRepository;
    private final MongoTemplate mongoTemplate;

    private final Integer MAX_MATCHING_KEYWORDS = 2;

    private static <T> T getByRandomClass(Set<T> set) {
        if (set == null || set.isEmpty()) {
            throw new IllegalArgumentException("The Set cannot be empty.");
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(set.size());
        return set.stream()
                .skip(randomIndex)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Something went wrong while picking a random element."));
    }

    @Caching(
            evict = {@CacheEvict(value = "productRecommendations", key = "#userHash", condition = "#forceRefresh", beforeInvocation = true)},
            cacheable = {@Cacheable(value = "productRecommendations", key = "#userHash", condition = "!#forceRefresh", unless = "#result == null or #result.isEmpty()")},
            put = {@CachePut(value = "productRecommendations", key = "#userHash", condition = "#forceRefresh")}
    )
    public List<Cosmetic> recommendProducts(String userId, String userHash, boolean forceRefresh) {
        User user = userService.findById(userId);

        Set<String> combinedCosmeticIds = new HashSet<>();

        String userBaumann = user.getBaumann();

        // Method 1: Reviews filtered by Baumann type
        combinedCosmeticIds.addAll(getCosmeticIdsByBaumann(userBaumann));

        // Method 2: Reviews filtered by NLP probabilities
        combinedCosmeticIds.addAll(getCosmeticIdsByProbability(userBaumann));

        // Method 3: Trending cosmetics
        combinedCosmeticIds.addAll(getTrendingCosmeticIds());

        Set<String> userFavs = user.getCosmeticIds();

        // Method 4: Remove cosmetics that user already have in fav.
        combinedCosmeticIds.removeAll(userFavs);

        if (!userFavs.isEmpty()) {
            // Method 5: Cosmetics favored by users with similar Baumann skin types (MAX_MATCHING_KEYWORDS)
            combinedCosmeticIds.addAll(findSimilarProducts(getByRandomClass(userFavs)));
        }

        // Retrieve cosmetics by the combined IDs
        return cosmeticRepository.findAllById(combinedCosmeticIds);
    }

    // Manual cache eviction method
//    private void evictCache(String userId) {
//        User user = userService.findById(userId);
//        String userHash = hashUserData(user);
//        Cache cache = cacheManager.getCache("productRecommendations");
//        if (cache != null) {
//            cache.evict(new SimpleKey(userId, userHash));
//        }
//    }

    // If user changes baumann or favourite items, it'll force refresh
    public String hashUserData(User user) {
        // Convert the set to a list, sort it to ensure order, and then calculate hash code
        List<String> sortedFavs = new ArrayList<>(user.getCosmeticIds());
        Collections.sort(sortedFavs);

        var userBaumann = user.getBaumann().isEmpty() ? "OSNT" : user.getBaumann();
        return user.getId().hashCode() + "-" + userBaumann.hashCode() + "-" + sortedFavs.hashCode();
    }

    private Set<String> getCosmeticIdsByBaumann(String baumannSkinType) {
        List<Review> filteredReviewsByBaumann = reviewService.getReviewsOfBaumann(3, baumannSkinType);

        // Convert to list to shuffle
        List<String> cosmeticIds = filteredReviewsByBaumann.stream()
                .map(review -> review.getCosmetic().getId())
                .collect(Collectors.toList());

        // Shuffle the list to randomize the order
        Collections.shuffle(cosmeticIds);

        return new HashSet<>(cosmeticIds); // Use LinkedHashSet to preserve order
    }

    private Set<String> getCosmeticIdsByProbability(String baumannSkinType) {
        // Get reviews filtered by the probability scores from the NLP analysis
        List<Review> probablyBaumannReviews = reviewService.getReviewsForRecommendation(3, baumannSkinType);

        return probablyBaumannReviews.stream()
                .map(review -> review.getCosmetic().getId())
                .collect(Collectors.toSet());
    }


//    private Set<String> getSimilarUsersFavorites(String baumannSkinType) {
//        // Implementation of fetching IDs of cosmetics favored by users with similar Baumann skin types
//    }
//
//    private Set<String> getPersonalizedPicks(String baumannSkinType) {
//        // Implementation of fetching IDs of personalized picks based on past reviews
//    }

    private Set<String> getTrendingCosmeticIds() {
        // Implementation of fetching IDs of trending cosmetics
        List<Cosmetic> cosmetics = cosmeticRankService.getTopRankedCosmetics(3);
        return convertCosmeticsToStrings(cosmetics);
    }

    private Set<String> findSimilarProducts(String productId) {
        Cosmetic originalCosmetic = cosmeticRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cosmetic not found"));

        List<String> originalKeywords = originalCosmetic.getKeywords();

        // Define the match criteria
        Criteria criteria = Criteria.where("keywords").in(originalKeywords)
                .and("_id").ne(new ObjectId(originalCosmetic.getId()));

        // Define the aggregation pipeline
        MatchOperation matchOperation = Aggregation.match(criteria);
        SampleOperation sampleOperation = Aggregation.sample(15);

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, sampleOperation);

        // Execute the aggregation
        AggregationResults<Cosmetic> results = mongoTemplate.aggregate(aggregation, Cosmetic.class, Cosmetic.class);
        List<Cosmetic> potentialMatches = results.getMappedResults();

        // Filter and sort the potential matches using parallel streams
        return potentialMatches.parallelStream()
                .map(cosmetic -> new AbstractMap.SimpleEntry<>(cosmetic, countMatchingKeywords(cosmetic.getKeywords(), originalKeywords)))
                .filter(entry -> entry.getValue() >= MAX_MATCHING_KEYWORDS)
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .limit(7)
                .map(Map.Entry::getKey)
                .map(Cosmetic::getId)
                .collect(Collectors.toSet());
    }

    private Set<String> convertCosmeticsToStrings(List<Cosmetic> cosmetics) {
        return cosmetics.stream()
                .map(Cosmetic::getId)
                .collect(Collectors.toSet());
    }

    private int countMatchingKeywords(List<String> keywords, List<String> originalKeywords) {
        Set<String> originalKeywordsSet = new HashSet<>(originalKeywords);
        return (int) keywords.stream()
                .filter(originalKeywordsSet::contains)
                .count();
    }
}