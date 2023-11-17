package app.beautyminder.repository;

import app.beautyminder.domain.KeywordRank;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface KeywordRankRepository extends MongoRepository<KeywordRank, String> {
    // Find the latest KeywordRank by sorting the createdAt field in descending order and limit the result to 1
    Optional<KeywordRank> findTopByOrderByCreatedAtDesc();
    Optional<KeywordRank> findTopByOrderByUpdatedAtDesc();

    Optional<KeywordRank> findByDate(LocalDate now);

    // Find the most recent KeywordRank before a given date
    Optional<KeywordRank> findTopByDateBeforeOrderByDateDesc(LocalDate date);
}