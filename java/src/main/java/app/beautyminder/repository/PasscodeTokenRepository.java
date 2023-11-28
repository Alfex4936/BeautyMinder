package app.beautyminder.repository;

import app.beautyminder.domain.PasscodeToken;
import app.beautyminder.domain.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.Optional;

public interface PasscodeTokenRepository extends MongoRepository<PasscodeToken, String> {

    Optional<PasscodeToken> findByToken(String token);
    Optional<PasscodeToken> findByEmail(String email);

    void deleteByEmail(String email);

    void deleteByExpiryDateBefore(Date currentDate);
}
