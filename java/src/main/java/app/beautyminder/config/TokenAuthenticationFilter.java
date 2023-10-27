package app.beautyminder.config;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.RefreshToken;
import app.beautyminder.domain.User;
import app.beautyminder.dto.user.LoginResponse;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.service.auth.RefreshTokenService;
import app.beautyminder.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

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

    private static final Pattern UNPROTECTED_SWAGGER_API =
            Pattern.compile("^/(swagger-ui|v3/api-docs|proxy)(/.*)?$");
    private static final Pattern UNPROTECTED_API =
            Pattern.compile("^/(es-index|data-view|gpt|search|cosmetic/hit|cosmetic/click|redis|user/sms/send|baumann)(/.*)?$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (!isProtectedRoute(request.getRequestURI())) { // early return
            logger.debug("Accessing unprotected route! " + request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        AtomicBoolean isAuth = new AtomicBoolean(true);

//        if (SecurityContextHolder.getContext().getAuthentication() == null) {
        String token = getAccessToken(request);

        if (tokenProvider.validToken(token)) {
            logger.debug("Valid access token found, setting the authentication.");
            Authentication auth = tokenProvider.getAuthentication(token);
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            logger.debug("User Authorities: {}", authorities);

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
                                Collection<? extends GrantedAuthority> authorities = newAuth.getAuthorities();
                                logger.debug("Ref User Authorities: {}", authorities);

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
//                                        objectMapper.writeValue(response.getWriter(), result);
                                    response.getOutputStream().write(result.getBytes());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            () -> {
                                logger.warn("Invalid refresh token. Clearing the security context.");

                                SecurityContextHolder.getContext().setAuthentication(null);
                                SecurityContextHolder.clearContext();

                                // block to explicitly handle unauthorized requests
                                logger.warn("Unauthorized request, returning 401");
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                                // Set the content type of the response to JSON
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");

                                // Create a JSON object with the error message
                                String jsonMessage = "{\"msg\":\"Unauthorized. Please provide a valid token.\"}";

                                // Write the JSON message to the response

                                try {
//                                        response.getWriter().write(jsonMessage);

                                    response.getOutputStream().write(jsonMessage.getBytes());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                isAuth.set(false);
                            });
        }
//        }

        if (isAuth.get()) {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isProtectedRoute(String uri) {
        return
                !UNPROTECTED_SWAGGER_API.matcher(uri).matches() &&
                        !UNPROTECTED_API.matcher(uri).matches() &&
                        Arrays.stream(unprotectedRoutes).noneMatch(uri::startsWith);
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

