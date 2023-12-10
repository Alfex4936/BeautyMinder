package app.beautyminder.config.chat;

import app.beautyminder.service.chat.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketEventListener {
    private final WebSocketSessionManager sessionManager;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        Map<String, Object> headers = event.getMessage().getHeaders();
        Map<String, Object> sessionAttributes = (Map<String, Object>) headers.get("simpSessionAttributes");

        String userName = (String) sessionAttributes.get("user");
        String sessionId = (String) headers.get("simpSessionId");
        if (sessionId != null && userName != null) {
            sessionManager.registerSession(sessionId, userName);
        } else {
            log.warn("Session ID or Principal is null.");
        }
    }

    @EventListener
    public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
//      log.info("BEMINDER: connected {}", event.getUser().getName());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        sessionManager.removeSession(event.getSessionId());
    }
}