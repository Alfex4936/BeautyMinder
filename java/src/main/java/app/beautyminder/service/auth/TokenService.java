package app.beautyminder.service.auth;

import app.beautyminder.domain.PasscodeToken;
import app.beautyminder.domain.PasswordResetToken;
import app.beautyminder.domain.User;
import app.beautyminder.repository.PasscodeTokenRepository;
import app.beautyminder.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    private final PasscodeTokenRepository passcodeTokenRepository;
    private final int LENGTH = 6;
    private final long VALID_HOURS = 2;
    private final long VALID_MINUTES = 5;

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

    public PasscodeToken createPasscode(String email) {
        String token = generateToken(LENGTH);
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(VALID_MINUTES);

        PasscodeToken passwordResetToken = passcodeTokenRepository.findByEmail(email)
                .map(existingToken -> {
                    existingToken.setToken(token);
                    existingToken.setExpiryDate(expiryDate);
                    existingToken.setVerified(false);
                    return existingToken;
                })
                .or(() -> Optional.of(PasscodeToken.builder()
                        .email(email)
                        .token(token)
                        .expiryDate(expiryDate)
                        .build()))
                .get();

        passcodeTokenRepository.save(passwordResetToken);

        return passwordResetToken;
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

    public boolean validateVerificationToken(String token) {
        Optional<PasscodeToken> passcodeTokenOpt = passcodeTokenRepository.findByToken(token);

        if (passcodeTokenOpt.isEmpty() || passcodeTokenOpt.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            // Token is invalid or expired
            return false;
        }

        PasscodeToken passcodeToken = passcodeTokenOpt.get();
        passcodeToken.setVerified(true); // Mark the email as verified
        passcodeTokenRepository.save(passcodeToken);

        return true; // Token is valid and email is marked as verified
    }

    public PasswordResetToken validateResetToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        return passwordResetToken;
    }

    public boolean isEmailVerified(String email) {
        return passcodeTokenRepository.findByEmail(email)
                .map(PasscodeToken::isVerified) // Maps the Optional<PasscodeToken> to Optional<Boolean>
                .orElse(false); // Returns false if the Optional is empty (i.e., no token found or email not verified)
    }

    public void removePassCodeFor(String email) {
        passcodeTokenRepository.deleteByEmail(email);
    }


    @Scheduled(cron = "0 0 2 * * WED", zone = "Asia/Seoul") // Delete all expired tokens at wednesday 2am
    public void deleteAllExpiredTokensOften() {
        passwordResetTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        passcodeTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}

