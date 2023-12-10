package app.beautyminder.service.chat;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionManager {
    private final ConcurrentHashMap<String, String> userSessionMap = new ConcurrentHashMap<>();

    public synchronized void registerSession(String sessionId, String username) {
        userSessionMap.put(sessionId, username);
    }

    public synchronized boolean isAlreadyConnected(String username) {
        return userSessionMap.containsValue(username);
    }

    public synchronized void removeSession(String sessionId) {
        userSessionMap.remove(sessionId);
    }

    public ConcurrentHashMap<String, String> getConnectedUsers() {
        return userSessionMap;
    }

}
