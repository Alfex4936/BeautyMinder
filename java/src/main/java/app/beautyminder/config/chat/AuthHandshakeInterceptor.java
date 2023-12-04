package app.beautyminder.config.chat;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.service.chat.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.WebUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static app.beautyminder.config.TokenAuthenticationFilter.*;

@Slf4j
@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final TokenProvider tokenProvider;
    private final WebSocketSessionManager sessionManager; // Inject the session manager

    public static String getAccessToken(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        List<String> authorizationHeader = headers.get(HEADER_AUTHORIZATION);
        log.info("BEMINDER: TOKENS: {}", authorizationHeader);


        if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
            String authHeader = authorizationHeader.get(0);
            if (authHeader.startsWith(TOKEN_PREFIX)) {
                return authHeader.substring(TOKEN_PREFIX.length());
            }
        }

        // check for cookies too
        if (request instanceof ServletServerHttpRequest servletRequest) {
            var accessCookie = WebUtils.getCookie(servletRequest.getServletRequest(), ACCESS_TOKEN_COOKIE);
            return Objects.requireNonNull(accessCookie).getValue();
        }

        return null;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = getAccessToken(servletRequest);
            if (token != null && tokenProvider.validToken(token)) {
                String username = tokenProvider.getUserEmail(token);

                // Check if user already has an active session
                if (sessionManager.isAlreadyConnected(username)) {
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    response.getHeaders().add("message", "403 duplicated name,");
                    return false; // Reject the handshake
                }

                var session = servletRequest.getServletRequest().getSession();
                sessionManager.registerSession(username, session.getId());
                attributes.put("username", username); // Storing the username in session attributes
            } else {
                return false; // Reject the handshake
            }
        }
        return true; // Proceed with the handshake if valid
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Post-handshake logic
    }

}