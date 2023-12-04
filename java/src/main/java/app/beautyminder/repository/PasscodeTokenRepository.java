package app.beautyminder.repository;

import app.beautyminder.domain.PasscodeToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasscodeTokenRepository extends MongoRepository<PasscodeToken, String> {

    Optional<PasscodeToken> findByToken(String token);

    Optional<PasscodeToken> findByEmail(String email);

    void deleteByEmail(String email);

    void deleteByExpiryDateBefore(LocalDateTime expiryDate);
}
