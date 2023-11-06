package app.beautyminder.repository;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByUser(User user);

    List<Review> findByCosmetic(Cosmetic cosmetic);

    @Query("{ 'rating' :  { $gte:  ?0 }, 'user.baumann' :  ?1 }")
    List<Review> findReviewsByRatingAndUserBaumann(Integer minRating, String userBaumann);

    @Aggregation(pipeline = {
            "{ $match : { 'rating': {$gte: ?0, $lte: ?1} } }",
            "{ $sample : { 'size' : ?2 } }"
    })
    List<Review> findRandomReviewsByRating(Integer minRating, Integer maxRating, Integer limit);

    @Aggregation(pipeline = {
            "{ $match : { 'rating': {$gte: ?0, $lte: ?1}, 'cosmetic._id': ?2 } }",
            "{ $sample : { 'size' : ?3 } }"
    })
    List<Review> findRandomReviewsByRatingAndCosmetic(Integer minRating, Integer maxRating, String cosmeticId, Integer limit);

    boolean existsByUserIdAndCosmeticId(String userId, String cosmeticId);
}