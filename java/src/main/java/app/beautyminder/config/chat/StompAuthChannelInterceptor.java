package app.beautyminder.config.chat;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.service.chat.WebSocketSessionManager;
import com.sun.security.auth.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;


// flutter stomp client in web cannot send custom header.
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;
    private final WebSocketSessionManager sessionManager;

    public StompAuthChannelInterceptor(TokenProvider tokenProvider, WebSocketSessionManager sessionManager) {
        this.tokenProvider = tokenProvider;
        this.sessionManager = sessionManager;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

//        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
//            sessionManager.removeSession(accessor.getUser().getName());
//        } else
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

            String token = (String) sessionAttributes.get("token");
            if (token.isEmpty()) {
                token = accessor.getFirstNativeHeader("access-token");
            }

            if (tokenProvider.validToken(token)) {
                String username = tokenProvider.getUserEmail(token);

                if (sessionManager.isAlreadyConnected(username)) {
                    throw new AccessDeniedException("Duplicate session.");
                }

                log.info("BEMINDER: username: {}", username);
//                sessionManager.registerSession(username, accessor.getSessionId());
                accessor.setSessionAttributes(Map.of("user", username));
                accessor.setUser(new UserPrincipal(username));
            } else {
//                accessor.setUser(new UserPrincipal("익명-" + UUID.randomUUID()));
            }
            // If no token is provided, just proceed without throwing an error
        }

        return message;
    }
}