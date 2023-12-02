package app.beautyminder.service;

import app.beautyminder.dto.chat.ChatMessage;
import app.beautyminder.dto.chat.ChatRoom;
import app.beautyminder.service.chat.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class ChatServiceTest {

    @Test
    public void findAllRoom_ShouldReturnAllRooms() {
        ChatService chatService = new ChatService();
        chatService.initPermanentRooms(); // Manually initialize rooms for the test

        List<ChatRoom> rooms = chatService.findAllRoom();

        assertNotNull(rooms);
        assertFalse(rooms.isEmpty());
    }

    @Test
    public void findRoomById_ShouldReturnCorrectRoom() {
        ChatService chatService = new ChatService();
        chatService.initPermanentRooms();
        List<ChatRoom> allRooms = chatService.findAllRoom();
        ChatRoom expectedRoom = allRooms.get(0); // Assuming a room exists

        ChatRoom result = chatService.findRoomById(expectedRoom.getRoomId());

        assertNotNull(result);
        assertEquals(expectedRoom.getRoomId(), result.getRoomId());
    }

    @Test
    public void updateRoomActivity_ShouldUpdateRoomActivity() throws InterruptedException {
        ChatService chatService = new ChatService();
        chatService.initPermanentRooms();
        List<ChatRoom> allRooms = chatService.findAllRoom();
        String roomId = allRooms.get(0).getRoomId();

        LocalDateTime beforeUpdate = chatService.findRoomById(roomId).getLastActiveTime();
        Thread.sleep(1000);
        LocalDateTime afterUpdate = chatService.findRoomById(roomId).getLastActiveTime();

        assertNotNull(afterUpdate);
        assertNotEquals(beforeUpdate, afterUpdate);
    }

    @Test
    public void sendMessageToRoom_ShouldAddMessageToRoom() {
        ChatService chatService = new ChatService();
        chatService.initPermanentRooms();
        List<ChatRoom> allRooms = chatService.findAllRoom();
        ChatRoom room = allRooms.get(0);
        ChatMessage message = new ChatMessage();

        chatService.sendMessageToRoom(room.getRoomId(), message);

        assertTrue(room.getMessages().contains(message));
    }

    @Test
    public void getRoomUserCount_ShouldReturnCorrectUserCount() {
        ChatService chatService = new ChatService();
        chatService.initPermanentRooms();
        List<ChatRoom> allRooms = chatService.findAllRoom();
        ChatRoom room = allRooms.get(0);

        Integer userCount = chatService.getRoomUserCount(room.getRoomId());

        assertNotNull(userCount);
        assertEquals(0, userCount); // Assuming no user has entered the room yet
    }

    @Test
    public void userEnteredAndLeftRoom_ShouldUpdateUserCount() {
        ChatService chatService = new ChatService();
        chatService.initPermanentRooms();
        List<ChatRoom> allRooms = chatService.findAllRoom();
        ChatRoom room = allRooms.get(0);

        chatService.userEnteredRoom(room.getRoomId());
        assertEquals(1, chatService.getRoomUserCount(room.getRoomId()));

        chatService.userLeftRoom(room.getRoomId());
        assertEquals(0, chatService.getRoomUserCount(room.getRoomId()));
    }

}