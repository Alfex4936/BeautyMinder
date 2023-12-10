package app.beautyminder.service;

import app.beautyminder.service.chat.WebSocketSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketSessionManagerTest {

    private WebSocketSessionManager webSocketSessionManager;

    @BeforeEach
    public void setUp() {
        webSocketSessionManager = new WebSocketSessionManager();
    }

    @Test
    public void testRegisterSession() {
        webSocketSessionManager.registerSession("session1", "user1");
        assertTrue(webSocketSessionManager.isAlreadyConnected("user1"));
    }

    @Test
    public void testIsAlreadyConnected() {
        assertFalse(webSocketSessionManager.isAlreadyConnected("user2"));
        webSocketSessionManager.registerSession("session2", "user2");
        assertTrue(webSocketSessionManager.isAlreadyConnected("user2"));
    }

    @Test
    public void testRemoveSession() {
        webSocketSessionManager.registerSession("session3", "user3");
        assertTrue(webSocketSessionManager.isAlreadyConnected("user3"));

        webSocketSessionManager.removeSession("session3");
        assertFalse(webSocketSessionManager.isAlreadyConnected("user3"));

        assertTrue(webSocketSessionManager.getConnectedUsers().isEmpty());
    }

    @Test
    public void testRemoveNonExistingSession() {
        assertDoesNotThrow(() -> webSocketSessionManager.removeSession("nonExistingUser"));
    }
}