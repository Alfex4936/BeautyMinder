package app.beautyminder.controller.chat;

import app.beautyminder.dto.chat.ChatMessage;
import app.beautyminder.service.chat.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class StompController {
    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;


    @MessageMapping("/chat.send/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage send(@DestinationVariable String roomId, @Payload String messageJson) throws Exception {
        ChatMessage chatMessage = objectMapper.readValue(messageJson, ChatMessage.class);

        // Format the current time in Korean timezone
        LocalDateTime now = LocalDateTime.now(TimeZone.getTimeZone("Asia/Seoul").toZoneId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        // Append the timestamp to the message
        chatMessage.setMessage("[" + formattedDateTime + "] " + chatMessage.getSender() + ": " + chatMessage.getMessage());

        chatService.sendMessageToRoom(roomId, chatMessage);

        return chatMessage;
    }

    @MessageMapping("/chat.enter/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage enterRoom(@DestinationVariable String roomId, @Payload String messageJson) throws Exception {
        ChatMessage chatMessage = objectMapper.readValue(messageJson, ChatMessage.class);
        List<ChatMessage> oldMessages = chatService.getRoomMessages(roomId);

        // TODO(2023-11-15)
        // Send old messages to the user who just entered
//        for (ChatMessage oldMessage : oldMessages) {
//            messagingTemplate.convertAndSendToUser(
//                    chatMessage.getSender(),
//                    "/topic/room/" + roomId,
//                    oldMessage
//            );
//        }

        chatMessage.setMessage(chatMessage.getSender() + " 님이 입장 하셨습니다.");

        // Optionally, broadcast that a user has entered the room
        chatService.sendMessageToRoom(roomId, chatMessage);
        chatService.userEnteredRoom(roomId);
//        messagingTemplate.convertAndSend("/topic/room/" + roomId, chatMessage);

        return chatMessage;
    }

    @MessageMapping("/chat.quit/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage quitRoom(@DestinationVariable String roomId, @Payload String messageJson) throws Exception {
        ChatMessage chatMessage = objectMapper.readValue(messageJson, ChatMessage.class);
        chatService.sendMessageToRoom(roomId, chatMessage);

        chatMessage.setMessage(chatMessage.getSender() + " 님이 퇴장 하셨습니다.");
        chatService.userLeftRoom(roomId);

        return chatMessage;
    }
}