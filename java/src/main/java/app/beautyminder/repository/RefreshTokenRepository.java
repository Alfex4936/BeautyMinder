package app.beautyminder.repository;

import app.beautyminder.domain.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    Optional<RefreshToken> findByUserId(String userId);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    @Query("{'expiresAt': {'$lt': ?0}}")
    List<RefreshToken> findAllExpiredTokens(LocalDateTime now);

    List<RefreshToken> findAllByUserId(String userId);

    void deleteByUserId(String userId);

    void deleteByRefreshToken(String refreshToken);
}