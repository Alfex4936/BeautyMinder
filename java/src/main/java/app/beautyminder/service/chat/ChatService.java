package app.beautyminder.service.chat;

import app.beautyminder.dto.chat.ChatMessage;
import app.beautyminder.dto.chat.ChatRoom;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final Map<String, ChatRoom> chatRooms = new LinkedHashMap<>();
    private final SimpMessageSendingOperations messagingTemplate;

    @PostConstruct
    private void initPermanentRooms() {
        // List of Baumann skin types or any other predefined room names
        var baumannTypes = List.of(
                "OSNT (번들민감)", "OSNW (주름민감)", "OSPT (색소민감)", "OSPW (복합고민)",
                "ORNT (튼튼번들)", "ORNW (튼튼주름)", "ORPT (튼튼색소)", "ORPW (관리필요)",
                "DSNT (건민감)", "DSNW (건조주름)", "DSPT (건색민감)", "DSPW (건조복합)",
                "DRNT (튼튼건조)", "DRNW (튼튼건조주름)", "DRPT (건조튼튼색소)", "DRPW (건조관리)", "BeautyMinder");

        var descriptions = List.of(
                "(Oily, Sensitive, Non-pigmented, Tight): \"번들번들하지만 민감해요. 탄력 있는 피부지만 주름은 걱정 없는 타입!\"",
                "(Oily, Sensitive, Non-pigmented, Wrinkled): \"기름지고 민감한 피부에 주름까지! 까다로운 피부의 소유자!\"",
                "(Oily, Sensitive, Pigmented, Tight): \"민감하고 색소 침착이 있는 타입, 그러나 탄력은 좋아요!\"",
                "(Oily, Sensitive, Pigmented, Wrinkled): \"기름진 피부에 민감함과 주름, 색소까지... 복합적인 고민을 가진 당신!\"",
                "(Oily, Resistant, Non-pigmented, Tight): \"기름기는 있지만 튼튼한 피부. 탄력 있고 건강해 보여요!\"",
                "(Oily, Resistant, Non-pigmented, Wrinkled): \"기름진 피부지만 주름이 고민인 당신, 건강한 피부 관리가 필요해요!\"",
                "(Oily, Resistant, Pigmented, Tight): \"기름기 있지만 피부는 강하고 탄력 있어요. 하지만 색소 침착 주의!\"",
                "(Oily, Resistant, Pigmented, Wrinkled): \"튼튼한 피부에도 주름과 색소는 걱정, 관리가 필요한 타입이에요.\"",
                "(Dry, Sensitive, Non-pigmented, Tight): \"건조하고 민감해요. 하지만 탄력은 유지하는 까다로운 피부 타입!\"",
                "(Dry, Sensitive, Non-pigmented, Wrinkled): \"건조하고 민감한 피부에 주름까지... 세심한 관리가 필요한 타입!\"",
                "(Dry, Sensitive, Pigmented, Tight): \"건조하고 민감한데 색소 침착도 있어요. 탄력은 좋지만 주의가 필요해요!\"",
                "(Dry, Sensitive, Pigmented, Wrinkled): \"건조하고 주름, 색소 침착까지... 복합적인 피부 고민을 가진 당신!\"",
                "(Dry, Resistant, Non-pigmented, Tight): \"건조하지만 강한 피부. 탄력 있고 건강한 모습을 유지해요!\"",
                "(Dry, Resistant, Non-pigmented, Wrinkled): \"건조하지만 튼튼한 피부! 하지만 주름 관리는 필수에요.\"",
                "(Dry, Resistant, Pigmented, Tight): \"건조하지만 튼튼하고 탄력 있는 피부. 색소 침착에 주의해야 해요.\"",
                "(Dry, Resistant, Pigmented, Wrinkled): \"건조하고 튼튼하지만 주름과 색소가 걱정되는 복합 피부 타입!\"",
                "모든 대화가 저장되는 관리자와 대화할 수 있는 방이에요.");

        IntStream.range(0, baumannTypes.size()).mapToObj(i ->
                        ChatRoom.builder()
                                .roomId(UUID.randomUUID().toString())
                                .name(baumannTypes.get(i))
                                .description(descriptions.get(i))
                                .build())
                .forEach(room -> chatRooms.put(room.getRoomId(), room));
    }

    public List<ChatRoom> findAllRoom() {
        return new ArrayList<>(chatRooms.values());
    }

    public ChatRoom findRoomById(String roomId) {
        updateRoomActivity(roomId);
        return chatRooms.get(roomId);
    }

    public ChatRoom createRoom(String name) {
        String randomId = UUID.randomUUID().toString();
        ChatRoom chatRoom = ChatRoom.builder().roomId(randomId).name(name).build();
        chatRooms.put(randomId, chatRoom);
        return chatRoom;
    }

    public void updateRoomActivity(String roomId) {
        ChatRoom room = chatRooms.get(roomId);
        if (room != null) {
            room.updateLastActiveTime();
        }
    }

    public void sendMessageToRoom(String roomId, ChatMessage message) {
        ChatRoom room = chatRooms.get(roomId);
        if (room != null) {
            room.getMessages().add(message);
        }
    }

    public Integer getRoomUserCount(String roomId) {
        ChatRoom room = chatRooms.get(roomId);
        if (room != null) {
            return room.getUserCounts();
        }
        return null;
    }

    public void userEnteredRoom(String roomId) {
        ChatRoom room = chatRooms.get(roomId);
        if (room != null) {
            room.increaseUser();
        }
    }

    public void userLeftRoom(String roomId) {
        ChatRoom room = chatRooms.get(roomId);
        if (room != null) {
            room.decreaseUser();
        }
    }

    public List<ChatMessage> getRoomMessages(String roomId) {
        ChatRoom room = chatRooms.get(roomId);
        if (room != null) {
            return room.getMessages();
        }
        return Collections.emptyList();
    }

//    @Scheduled(fixedDelay = 60000) // Runs every 60 seconds, adjust as needed
//    public void cleanupInactiveRooms() {
//        LocalDateTime now = LocalDateTime.now();
//        chatRooms.entrySet().removeIf(entry -> {
//            ChatRoom room = entry.getValue();
//            return room.getSessions().isEmpty() &&
//                    Duration.between(room.getLastActiveTime(), now).toMinutes() >= 5; // 5 minutes of inactivity
//        });
//    }
}