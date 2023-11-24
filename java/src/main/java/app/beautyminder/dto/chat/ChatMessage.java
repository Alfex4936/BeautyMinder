package app.beautyminder.dto.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {
    private MessageType type; // 메시지 타입
    private String roomId; // 방번호
    private String sender; // 메시지 보낸사람
    private String message; // 메시지
    // 메시지 타입 : 입장, 채팅, 나감
    public enum MessageType {
        ENTER, TALK, QUIT, NOTICE
    }

    @Builder
    public ChatMessage(String roomId, String sender, String message, MessageType type) {
        this.roomId = roomId;
        this.sender = sender;
        this.message = message;
        this.type = type;
    }
}