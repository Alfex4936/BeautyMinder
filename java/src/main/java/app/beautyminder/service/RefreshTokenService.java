package app.beautyminder.service;

import app.beautyminder.domain.RefreshToken;
import app.beautyminder.domain.User;
import app.beautyminder.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected token"));
    }

    public Optional<User> findUserByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .map(RefreshToken::getUser);
    }

    public void deleteAllExpiredTokens(LocalDateTime now) {
        List<RefreshToken> expiredTokens = refreshTokenRepository.findAllExpiredTokens(now);
        refreshTokenRepository.deleteAll(expiredTokens);
    }

    public void updateRefreshTokenByUserId(String userId, String newToken) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId);
        for (RefreshToken token : tokens) {
            token.update(newToken);
        }
        refreshTokenRepository.saveAll(tokens);
    }
}