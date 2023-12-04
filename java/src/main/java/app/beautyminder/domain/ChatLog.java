package app.beautyminder.domain;

import app.beautyminder.dto.chat.ChatMessage;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "chats")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@Setter
public class ChatLog {

    @Id
    private String id;

    private String roomName;

    private List<Message> messages;

    // Inner class to represent individual messages

    public void addMessage(Message msg) {
        this.messages.add(msg);
    }

    @Getter
    @Setter
    @Builder
    public static class Message {
        private String sender;
        private String content;
        private long timestamp;
        private ChatMessage.MessageType type; // 메시지 타입

    }
}
