package app.beautyminder.controller;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.RefreshToken;
import app.beautyminder.domain.User;
import app.beautyminder.dto.CreateAccessTokenRequest;
import app.beautyminder.dto.CreateAccessTokenResponse;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.service.auth.RefreshTokenService;
import app.beautyminder.service.auth.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static app.beautyminder.config.WebSecurityConfig.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/token") // /token/api
public class TokenApiController {

    private final TokenService tokenService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/new")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(@RequestBody CreateAccessTokenRequest request) {
        String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(newAccessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        Optional<String> optionalRefreshToken = getRefreshTokenFromRequest(request);

        if (optionalRefreshToken.isPresent() && tokenProvider.validToken(optionalRefreshToken.get())) {
            User user = refreshTokenService.findUserByRefreshToken(optionalRefreshToken.get())
                    .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

            String newAccessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
            String newRefreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);

            // save refreshToken in db
            saveRefreshToken(user, newRefreshToken);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", newRefreshToken);


            return new ResponseEntity<>(tokens, HttpStatus.OK);
        }

        return new ResponseEntity<>("Invalid refresh token", HttpStatus.UNAUTHORIZED);
    }

    private Optional<String> getRefreshTokenFromRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(c -> REFRESH_TOKEN_COOKIE_NAME.equals(c.getName()))
                        .findFirst())
                .map(Cookie::getValue);
    }

    private void saveRefreshToken(User user, String newRefreshToken) {
        LocalDateTime expiresAt = LocalDateTime.now().plus(REFRESH_TOKEN_DURATION);

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .map(entity -> {
                    entity.update(newRefreshToken);
                    entity.setExpiresAt(expiresAt);
                    return entity;
                })
                .orElse(new RefreshToken(user, newRefreshToken, expiresAt));

        refreshTokenRepository.save(refreshToken);
    }
}
