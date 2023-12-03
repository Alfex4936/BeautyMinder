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
        webSocketSessionManager.registerSession("user1", "session1");
        assertTrue(webSocketSessionManager.isAlreadyConnected("user1"));
    }

    @Test
    public void testIsAlreadyConnected() {
        assertFalse(webSocketSessionManager.isAlreadyConnected("user2"));
        webSocketSessionManager.registerSession("user2", "session2");
        assertTrue(webSocketSessionManager.isAlreadyConnected("user2"));
    }

    @Test
    public void testRemoveSession() {
        webSocketSessionManager.registerSession("user3", "session3");
        assertTrue(webSocketSessionManager.isAlreadyConnected("user3"));

        webSocketSessionManager.removeSession("user3");
        assertFalse(webSocketSessionManager.isAlreadyConnected("user3"));
    }

    @Test
    public void testRemoveNonExistingSession() {
        assertDoesNotThrow(() -> webSocketSessionManager.removeSession("nonExistingUser"));
    }
}