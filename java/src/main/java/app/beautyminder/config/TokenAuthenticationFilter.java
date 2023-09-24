package app.beautyminder.config;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.RefreshToken;
import app.beautyminder.domain.User;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.service.RefreshTokenService;
import app.beautyminder.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static app.beautyminder.config.WebSecurityConfig.*;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

//        String path = request.getRequestURI();

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = getAccessToken(request);

            if (tokenProvider.validToken(token)) {
                Authentication auth = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                // Attempt to use refresh token
                Optional<String> optionalRefreshToken = getRefreshTokenFromRequest(request);

                optionalRefreshToken
                        .filter(tokenProvider::validToken)
                        .flatMap(refreshTokenService::findUserByRefreshToken)
                        .ifPresentOrElse(
                                user -> {
                                    System.out.println("====== New token");
                                    // Generate new access token and refresh token
                                    String newAccessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
                                    String newRefreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);

                                    // Update the Security Context
                                    Authentication newAuth = tokenProvider.getAuthentication(newAccessToken);
                                    SecurityContextHolder.getContext().setAuthentication(newAuth);

                                    // Update the HTTP headers and cookies
                                    response.addHeader(HEADER_AUTHORIZATION, TOKEN_PREFIX + newAccessToken);

                                    int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
                                    CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
                                    CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, newRefreshToken, cookieMaxAge);

                                    // Optionally, update the refresh token in the database
                                    saveRefreshToken(user, newRefreshToken);
                                },
                                () -> {
                                    System.out.println("====== None");
                                    SecurityContextHolder.getContext().setAuthentication(null);
                                    SecurityContextHolder.clearContext();
                                });
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }

        // Also check for token in cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("BEARER_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private Optional<String> getRefreshTokenFromRequest(HttpServletRequest request) {
        // Similar to your existing `getRefreshTokenFromRequest` method
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

