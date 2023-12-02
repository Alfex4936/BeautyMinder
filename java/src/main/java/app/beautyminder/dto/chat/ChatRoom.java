package app.beautyminder.dto.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class ChatRoom {
    private final String roomId;
    private final String name;
    private final String description;
    private final Set<WebSocketSession> sessions = new HashSet<>();

    private final List<ChatMessage> messages = new ArrayList<>();

    @Setter
    private Integer userCounts = 0;

    @Setter
    private Set<String> nickNames = new HashSet<>();

    @Setter
    private LocalDateTime lastActiveTime;

    @Builder
    public ChatRoom(String roomId, String name, String description) {
        this.roomId = roomId;
        this.name = name;
        this.description = description;
        this.lastActiveTime = LocalDateTime.now();
    }

    public void updateLastActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
    }

    public void addNickName(String nickName) {
        this.nickNames.add(nickName);
    }

    public boolean isDuplicatedName(String nickName) {
        return this.nickNames.contains(nickName);
    }

    public Integer increaseUser() {
        return this.userCounts++;
    }

    public Integer decreaseUser() {
        if (this.userCounts == 0) {
            return 0;
        }
        return this.userCounts--;
    }
}