package app.beautyminder.service.auth;

import app.beautyminder.domain.PasswordResetToken;
import app.beautyminder.domain.User;
import app.beautyminder.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class TokenService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final int LENGTH = 6;
    private final long VALID_HOURS = 2;

    public static String generateToken(int length) {
        StringBuilder token = new StringBuilder(length);

        IntStream.range(0, length).forEach(
                i -> {
                    var index = RANDOM.nextInt(CHARACTERS.length());
                    token.append(CHARACTERS.charAt(index));
                }
        );
//        for (int i = 0; i < length; i++) {
//            int index = RANDOM.nextInt(CHARACTERS.length());
//            token.append(CHARACTERS.charAt(index));
//        }
        return token.toString();
    }

    public PasswordResetToken createPasswordResetToken(User user) {
        String token = generateToken(LENGTH);
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(VALID_HOURS);

        // Using Optional's or() method for cleaner code
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByEmail(user.getEmail())
                .map(existingToken -> {
                    existingToken.setToken(token);
                    existingToken.setExpiryDate(expiryDate);
                    return existingToken;
                })
                .or(() -> Optional.of(PasswordResetToken.builder()
                        .email(user.getEmail())
                        .token(token)
                        .expiryDate(expiryDate)
                        .build()))
                .get();

        passwordResetTokenRepository.save(passwordResetToken);

        return passwordResetToken;
    }

    public PasswordResetToken validateResetToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        return passwordResetToken;
    }


    @Scheduled(cron = "0 0 2 * * WED", zone = "Asia/Seoul") // Delete all expired tokens at wednesday 2am
    public void deleteAllExpiredTokensOften() {
        passwordResetTokenRepository.deleteByExpiryDateBefore(new Date());
    }
}

