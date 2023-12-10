package app.beautyminder.repository;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {

    @Query(value = "{ 'isFiltered': true }")
    Page<Review> findAllFiltered(Pageable pageable);

    @Query(value = "{ 'user.$id': ?0 }")
    List<Review> findByUserId(ObjectId userId);

    List<Review> findByUser(User user);

    List<Review> findByCosmetic(Cosmetic cosmetic);

    Page<Review> findByCosmetic(Cosmetic cosmetic, Pageable pageable);

    Page<Review> findByCosmeticAndUserNot(Cosmetic cosmetic, User user, Pageable pageable);

    @Aggregation(pipeline = {
            "{ $match : { 'rating': {$gte: ?0, $lte: ?1} } }",
            "{ $sample : { 'size' : ?2 } }"
    })
    List<Review> findRandomReviewsByRating(Integer minRating, Integer maxRating, Integer limit);

    @Query("{ 'rating' :  { $gte: ?0 }, 'user.$id' : { $in: ?1 } }")
    List<Review> findReviewsByRatingAndUserIds(Integer minRating, List<ObjectId> userIds, Pageable pageable);

    @Aggregation(pipeline = {
            "{ $match : { 'rating': {$gte: ?0, $lte: ?1}, 'cosmetic.$id': ?2 } }",
            "{ $addFields : { sortFields: { $ifNull: ['$updatedAt', '$createdAt'] } } }", // Sort by updatedAt if available, otherwise by createdAt
            "{ $sort : { sortField: -1, _id: 1} }", // Limit the results to the specified size
            "{ $limit : 15 }", // Limit the results to the specified size
            "{ $sample : { 'size' : ?3 } }"
    })
    List<Review> findRandomReviewsByRatingAndCosmetic(Integer minRating, Integer maxRating, ObjectId cosmeticId, Integer limit);

    @Query(value = "{ 'user.$id': ?0, 'cosmetic.$id': ?1 }")
    Optional<Review> findByUserIdAndCosmeticId(ObjectId userId, ObjectId cosmeticId);

    @Query(value = "{ 'user.$id': ?0 }", delete = true)
        // delete ALL
    void deleteByUserId(ObjectId userId);
}