package app.beautyminder.config.chat;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.service.chat.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@RequiredArgsConstructor
@Component
public class WebSocketEventListener {
    private final WebSocketSessionManager sessionManager;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String username = Objects.requireNonNull(event.getUser()).getName(); // Extract the username from the event
        sessionManager.removeSession(username);
    }
}