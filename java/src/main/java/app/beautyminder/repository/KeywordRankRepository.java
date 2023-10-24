package app.beautyminder.repository;

import app.beautyminder.domain.KeywordRank;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KeywordRankRepository extends MongoRepository<KeywordRank, String> {

}