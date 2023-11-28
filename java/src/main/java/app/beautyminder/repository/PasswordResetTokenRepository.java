package app.beautyminder.repository;

import app.beautyminder.domain.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByEmail(String email);

    void deleteByEmail(String email);

    void deleteByExpiryDateBefore(LocalDateTime expiryDate);
}
