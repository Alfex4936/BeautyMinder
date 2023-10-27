package app.beautyminder.controller.user;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.RefreshToken;
import app.beautyminder.domain.User;
import app.beautyminder.dto.CreateAccessTokenRequest;
import app.beautyminder.dto.CreateAccessTokenResponse;
import app.beautyminder.dto.user.SignUpResponse;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.service.auth.RefreshTokenService;
import app.beautyminder.service.auth.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
public class TokenController {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Operation(
            summary = "Refresh Tokens",
            description = "Refresh Token 재발급.",
            tags = {"Token Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "RefreshToken 생성됨", content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
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
