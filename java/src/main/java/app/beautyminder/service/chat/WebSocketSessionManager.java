package app.beautyminder.service.chat;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionManager {
    private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

    public synchronized void registerSession(String username, String sessionId) {
        userSessionMap.put(username, sessionId);
    }

    public synchronized boolean isAlreadyConnected(String username) {
        return userSessionMap.containsKey(username);
    }

    public synchronized void removeSession(String username) {
        userSessionMap.remove(username);
    }
}
