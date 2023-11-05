package app.beautyminder.service;

import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import app.beautyminder.service.cosmetic.CosmeticSearchService;
import app.beautyminder.service.cosmetic.ReviewSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendService {

    /*
 GPT-4 프롬프트 엔지니어링을 활용하여 각 사용자의 바우만 피부 타입에 가장 적합한 키워드 n개를 추출한다.
 이렇게 도출된 키워드는 제품 검색의 기준이 되며, ElasticSearch으로 제품 검색 기능을 통해 각 키워드에 대한 겸색으로 나타난 제품들 중
 랜덤하게 k개씩을 선정하여, 총 x개의 제품을 리스트업한다. 통계적 무작위성을 기반으로 하여 다양성과 폭넓은 선택지를 보장한다.
 다음 단계는 사용자와 동일한 바우만 피부 타입을 가진 다른 사용자들의 평가를 반영하는 것이다.
 3점 이상의 높은 평점을 받은 제품들을 y개 선별하여 기존 리스트에 추가하면 사용자의 피부 타입에 맞는 제품뿐만 아니라,
 실제로 긍정적인 피드백을 받은 제품들도 포함되어 추천 리스트의 신뢰도가 높아진다. 마지막으로,
 이렇게 구성된 x+y개의 제품 리스트 중에서 최종적으로 z개의 제품을 선별하여 사용자에게 추천해 알고리즘에 내재된
 여러 변수들을 종합적으로 분석하여 진행되며, 결과적으로 사용자에게 개인적인 특성과 필요에 가장 부합하는 제품들을 제시하게 된다.
     */

    private final CosmeticSearchService cosmeticSearchService;
    private final CosmeticRankService cosmeticRankService;
    private final ReviewSearchService reviewSearchService;
    private final ReviewService reviewService;
    private final CosmeticRepository cosmeticRepository;

//    public List<Cosmetic> recommendProducts(String baumannSkinType) {
//        Set<String> combinedCosmeticIds = new HashSet<>();
//
//        // Method 1: Reviews filtered by Baumann type
//        combinedCosmeticIds.addAll(getCosmeticIdsByBaumann(baumannSkinType));
//
//        // Method 2: Reviews filtered by NLP probabilities
//        combinedCosmeticIds.addAll(getCosmeticIdsByProbability(baumannSkinType));
//
//        // Method 3: Trending cosmetics
//        combinedCosmeticIds.addAll(getTrendingCosmeticIds());
//
//        // Method 4: Cosmetics favored by users with similar Baumann skin types
//        combinedCosmeticIds.addAll(getSimilarUsersFavorites(baumannSkinType));
//
//        // Method 6: Personalized picks based on past reviews
//        combinedCosmeticIds.addAll(getPersonalizedPicks(baumannSkinType));
//
//        // Method : Remove cosmetics that user already have in pouch.
//        combinedCosmeticIds.removeAll();
//
//        // Retrieve cosmetics by the combined IDs
//        return cosmeticRepository.findAllById(combinedCosmeticIds);
//    }
//
//
//    private Set<String> getCosmeticIdsByBaumann(String baumannSkinType) {
//        List<Review> filteredReviewsByBaumann = reviewService.getReviewsOfBaumann(3, baumannSkinType);
//        return filteredReviewsByBaumann.stream()
//                .map(review -> review.getCosmetic().getId())
//                .collect(Collectors.toSet());
//    }
//
//    private Set<String> getCosmeticIdsByProbability(String baumannSkinType) {
//        // Get reviews filtered by the probability scores from the NLP analysis
//        List<Review> probablyBaumannReviews = reviewService.getReviewsForRecommendation(3, baumannSkinType);
//        return probablyBaumannReviews.stream()
//                .map(review -> review.getCosmetic().getId())
//                .collect(Collectors.toSet());
//    }
//
//    private Set<String> getTrendingCosmeticIds() {
//        // Implementation of fetching IDs of trending cosmetics
//    }
//
//    private Set<String> getSimilarUsersFavorites(String baumannSkinType) {
//        // Implementation of fetching IDs of cosmetics favored by users with similar Baumann skin types
//    }
//
//    private Set<String> getPersonalizedPicks(String baumannSkinType) {
//        // Implementation of fetching IDs of personalized picks based on past reviews
//    }

}