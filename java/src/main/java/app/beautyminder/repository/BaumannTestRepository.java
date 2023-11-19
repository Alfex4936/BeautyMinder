package app.beautyminder.repository;

import app.beautyminder.domain.BaumannTest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BaumannTestRepository extends MongoRepository<BaumannTest, String> {
    List<BaumannTest> findByUserIdOrderByDateAsc(String userId);
}