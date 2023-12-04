package app.beautyminder.config.chat;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.service.chat.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TokenProvider tokenProvider;
    private final WebSocketSessionManager sessionManager; // Inject the session manager

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registering the endpoint and enabling SockJS
        registry.addEndpoint("/ws/chat").setHandshakeHandler(new ClientHandshakeHandler()).setAllowedOriginPatterns("*").withSockJS()
                .setInterceptors(new AuthHandshakeInterceptor(tokenProvider, sessionManager));
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Configure a message broker
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }
}