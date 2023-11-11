package app.beautyminder.service;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    public List<Cosmetic> recommendProducts(String userId) {
        User user = userService.findById(userId);

        Set<String> combinedCosmeticIds = new HashSet<>();

        // Method 1: Reviews filtered by Baumann type
        combinedCosmeticIds.addAll(getCosmeticIdsByBaumann(user.getBaumann()));

        // Method 2: Reviews filtered by NLP probabilities
        combinedCosmeticIds.addAll(getCosmeticIdsByProbability(user.getBaumann()));

        // Method 3: Trending cosmetics
        combinedCosmeticIds.addAll(getTrendingCosmeticIds());

        Set<String> userFavs = user.getCosmeticIds();

        // Method 4: Remove cosmetics that user already have in fav.
        combinedCosmeticIds.removeAll(userFavs);

        if (!userFavs.isEmpty()) {
            // Method 5: Cosmetics favored by users with similar Baumann skin types
            combinedCosmeticIds.addAll(findSimilarProducts(getByRandomClass(userFavs), 2));
        }

        // Retrieve cosmetics by the combined IDs
        return cosmeticRepository.findAllById(combinedCosmeticIds);
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
        List<Cosmetic> cosmetics = cosmeticRankService.getTopRankedCosmetics(5);
        return cosmetics.stream()
                .map(Cosmetic::getId)
                .collect(Collectors.toSet());
    }

    private Set<String> findSimilarProducts(String productId, int minKeywordMatch) {
        Cosmetic cosmetic = cosmeticRepository.findById(productId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cosmetic not found"));

        // Match operation to exclude the product itself
        MatchOperation matchStage = Aggregation.match(Criteria.where("_id").ne(productId));

        // Projection operation to compute the size of the intersection of keywords
        ProjectionOperation projectStage = Aggregation.project()
                .andExclude("_id")
                .andInclude("name", "brand", "images", "glowpick_url", "category", "status", "createdAt", "purchasedDate", "expirationDate") // Include other necessary fields
                .andExpression("size(setIntersection(keywords, ?0))", cosmetic.getKeywords()).as("matchingKeywordCount");

        // Match operation to filter documents with at least N matching keywords
        MatchOperation matchKeywordsStage = Aggregation.match(Criteria.where("matchingKeywordCount").gte(minKeywordMatch));

        // Build the aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                projectStage,
                matchKeywordsStage,
                Aggregation.sort(Sort.Direction.DESC, "matchingKeywordCount"), // Sort by the count of matching keywords
                Aggregation.limit(5) // Limit to 5 similar products
        );

        // Execute the aggregation
        AggregationResults<Cosmetic> results = mongoTemplate.aggregate(aggregation, "cosmetics", Cosmetic.class);

        return results.getMappedResults().stream().map(Cosmetic::getId).collect(Collectors.toSet());
    }
}