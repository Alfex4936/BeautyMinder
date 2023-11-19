package app.beautyminder.chat;

import app.beautyminder.config.TokenAuthenticationFilter;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static app.beautyminder.config.TokenAuthenticationFilter.HEADER_AUTHORIZATION;
import static app.beautyminder.config.TokenAuthenticationFilter.TOKEN_PREFIX;
import static app.beautyminder.config.WebSecurityConfig.REFRESH_TOKEN_COOKIE_NAME;

@Slf4j
@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {



    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // Extract JWT token from the request header and validate it
        // If valid, put the authentication object in the attributes map
        var token = getAccessToken(request);
        log.info("BEMINDER: token is {}", token);

        return true; // Proceed with the handshake if valid
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Post-handshake logic
    }

    public static String getAccessToken(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        List<String> authorizationHeader = headers.get(HEADER_AUTHORIZATION);

        if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
            String authHeader = authorizationHeader.get(0);
            if (authHeader.startsWith(TOKEN_PREFIX)) {
                return authHeader.substring(TOKEN_PREFIX.length());
            }
        }

        return null;
    }

}