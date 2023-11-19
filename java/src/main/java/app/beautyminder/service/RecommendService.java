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
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
            // Method 5: Cosmetics favored by users with similar Baumann skin types (MAX_MATCHING_KEYWORDS)
            combinedCosmeticIds.addAll(findSimilarProducts(getByRandomClass(userFavs)));
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
        SampleOperation sampleOperation = Aggregation.sample(10);

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, sampleOperation);

        // Execute the aggregation
        AggregationResults<Cosmetic> results = mongoTemplate.aggregate(aggregation, Cosmetic.class, Cosmetic.class);
        List<Cosmetic> potentialMatches = results.getMappedResults();

        // Filter and sort the potential matches
        List<Cosmetic> similarProducts = potentialMatches.stream()
                .filter(cosmetic -> countMatchingKeywords(cosmetic.getKeywords(), originalKeywords) >= MAX_MATCHING_KEYWORDS)
                .sorted(Comparator.comparingInt(cosmetic -> countMatchingKeywords(cosmetic.getKeywords(), originalKeywords)))
                .limit(7)
                .toList();

        return similarProducts.stream()
                .map(Cosmetic::getId)
                .collect(Collectors.toSet());
    }

    private Set<String> convertCosmeticsToStrings(List<Cosmetic> cosmetics) {
        return cosmetics.stream()
                .map(Cosmetic::getId)
                .collect(Collectors.toSet());
    }

    private int countMatchingKeywords(List<String> keywords, List<String> originalKeywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (originalKeywords.contains(keyword)) {
                count++;
            }
        }
        return count;
    }
}