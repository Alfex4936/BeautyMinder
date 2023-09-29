package app.beautyminder.config;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.RefreshToken;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.LoginResponse;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.service.RefreshTokenService;
import app.beautyminder.util.CookieUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static app.beautyminder.config.WebSecurityConfig.*;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    @Value("${unprotected.routes}")
    private String[] unprotectedRoutes;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

//        String path = request.getRequestURI();

        AtomicBoolean isAuth = new AtomicBoolean(true);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = getAccessToken(request);

            if (tokenProvider.validToken(token)) {
                logger.debug("Valid access token found, setting the authentication.");

                Authentication auth = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                logger.debug("Invalid access token. Attempting refresh.");

                // Attempt to use refresh token
                Optional<String> optionalRefreshToken = getRefreshTokenFromRequest(request);

                optionalRefreshToken
                        .filter(tokenProvider::validToken)
                        .flatMap(refreshTokenService::findUserByRefreshToken)
                        .ifPresentOrElse(
                                user -> {
                                    // here should I call /token/refresh ?
                                    logger.info("Valid refresh token found, generating new access tokens.");

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
//                                    response.addHeader("Authorization", "Bearer " + newAccessToken);

                                    response.setContentType("application/json");
                                    response.setCharacterEncoding("utf-8");
                                    LoginResponse login = new LoginResponse(newAccessToken, newRefreshToken, user);

                                    try {
                                        String result = objectMapper.registerModule(new JavaTimeModule()).writeValueAsString(login);
                                        response.getWriter().write(result);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                },
                                () -> {
                                    logger.warn("Invalid refresh token. Clearing the security context.");

                                    SecurityContextHolder.getContext().setAuthentication(null);
                                    SecurityContextHolder.clearContext();

                                    // block to explicitly handle unauthorized requests
                                    if (isProtectedRoute(request.getRequestURI())) {
                                        logger.warn("Unauthorized request, returning 401");
                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                                        // Set the content type of the response to JSON
                                        response.setContentType("application/json");
                                        response.setCharacterEncoding("UTF-8");

                                        // Create a JSON object with the error message
                                        String jsonMessage = "{\"msg\":\"Unauthorized. Please provide a valid token.\"}";

                                        // Write the JSON message to the response
                                        try {
                                            response.getWriter().write(jsonMessage);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }

                                        // Don't continue the filter chain
                                        isAuth.set(false);
                                    }
                                });
            }


        }

        if (isAuth.get()) {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isProtectedRoute(String uri) {
        return Arrays.stream(unprotectedRoutes).noneMatch(uri::startsWith);
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
        // First, try to get the refresh token from the header
        String headerToken = request.getHeader("XRT");
        if (headerToken != null && !headerToken.isEmpty()) {
            return Optional.of(headerToken);
        }

        // If the header didn't contain the token, try to get it from the cookies
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

