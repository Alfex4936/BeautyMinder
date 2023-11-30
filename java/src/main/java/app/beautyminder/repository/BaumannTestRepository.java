package app.beautyminder.repository;

import app.beautyminder.domain.BaumannTest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BaumannTestRepository extends MongoRepository<BaumannTest, String> {
    List<BaumannTest> findByUserIdOrderByDateAsc(String userId);

    @Transactional
    void deleteByIdAndUserId(String id, String userId);
}