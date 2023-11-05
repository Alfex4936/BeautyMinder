package app.beautyminder.repository;

import app.beautyminder.domain.KeywordRank;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface KeywordRankRepository extends MongoRepository<KeywordRank, String> {
    // Find the latest KeywordRank by sorting the createdAt field in descending order and limit the result to 1
    Optional<KeywordRank> findTopByOrderByCreatedAtDesc();

    Optional<KeywordRank> findByDate(LocalDate now);
}