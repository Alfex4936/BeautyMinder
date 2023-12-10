package app.beautyminder.controller.chat;

import app.beautyminder.domain.ChatLog;
import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.dto.chat.ChatMessage;
import app.beautyminder.dto.chat.ChatRoom;
import app.beautyminder.repository.ChatLogRepository;
import app.beautyminder.service.chat.ChatService;
import app.beautyminder.service.chat.WebSocketSessionManager;
import app.beautyminder.service.cosmetic.GPTService;
import app.beautyminder.service.review.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vane.badwordfiltering.BadWordFiltering;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompController {
    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ReviewService reviewService;
    private final GPTService gptService;
    private final ChatLogRepository chatLogRepository;
    private final WebSocketSessionManager sessionManager;

    private final Random random = new Random();
    private final BadWordFiltering badWordFiltering;

    private final Map<String, CompletableFuture<Void>> roomFutures = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Void>> gptFutures = new ConcurrentHashMap<>();


    @PreDestroy
    public void onDestroy() {
        roomFutures.values().forEach(this::cancelFuture);
        gptFutures.values().forEach(this::cancelFuture);
    }

    public List<String> getCurrentUsers() {
        ConcurrentHashMap<String, String> userSessionMap = sessionManager.getConnectedUsers();
        return new ArrayList<>(userSessionMap.values());
    }


    // SimpMessageHeaderAccessor accessor
    // enter -> if BM, send to /chat.save -> send back to /topic/room
    @MessageMapping("/chat.save/{roomId}")
    public void sendSavedMsg(@DestinationVariable String roomId) {
        var room = chatService.findRoomById(roomId);
        var optRoom = chatLogRepository.findByRoomName(room.getName());

        optRoom.ifPresent(chatLog -> {
            if (chatLog.getMessages().size() == 1) { // on first user entered a room for the first time
                return;
            }
            List<ChatMessage> savedChats = chatLog.getMessages().stream().map(message ->
                    ChatMessage.builder()
                            .sender(message.getSender())
                            .message(message.getContent())
                            .roomId(roomId)
                            .type(message.getType())
                            .build()
            ).collect(Collectors.toList());

            messagingTemplate.convertAndSend("/topic/room/batch/" + roomId, savedChats);
        });
    }

    @MessageMapping("/chat.send/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage send(@DestinationVariable String roomId, @Payload String messageJson) throws Exception {
        ChatMessage chatMessage = objectMapper.readValue(messageJson, ChatMessage.class);

        // Format the current time in Korean timezone
        LocalDateTime now = LocalDateTime.now(TimeZone.getTimeZone("Asia/Seoul").toZoneId());
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String fullFormattedDateTime = now.format(fullFormatter);
        String shortFormattedDateTime = now.format(shortFormatter);

        // Append the full timestamp for the desktop and the short one for mobile clients
        chatMessage.setMessage(
                "<span class=\"timestamp-full\">" + "[" + fullFormattedDateTime + "]" + "</span>" +
                        "<span class=\"timestamp-short\">" + "[" + shortFormattedDateTime + "]" + "</span>" +
                        chatMessage.getSender() + ": " +
                        badWordFiltering.change(chatMessage.getMessage())
        );


        addToDatabase(roomId, chatMessage);

        chatService.sendMessageToRoom(roomId, chatMessage);

        return chatMessage;
    }

    @MessageMapping("/chat.enter/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage enterRoom(@DestinationVariable String roomId, @Payload String messageJson) throws Exception {
        ChatMessage chatMessage = objectMapper.readValue(messageJson, ChatMessage.class);

        chatMessage.setMessage(chatMessage.getSender() + " 님이 입장 하셨습니다.");
        chatService.userEnteredRoom(roomId);

        addToDatabase(roomId, chatMessage);

        messagingTemplate.convertAndSend("/topic/room/name/" + roomId, Map.of("title", chatService.getRoomUserCount(roomId)));
        messagingTemplate.convertAndSend("/topic/room/currentUsers", getCurrentUsers());


        return chatMessage;
    }

    @MessageMapping("/chat.quit/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage quitRoom(@DestinationVariable String roomId, @Payload String messageJson, SimpMessageHeaderAccessor accessor) throws Exception {
        ChatMessage chatMessage = objectMapper.readValue(messageJson, ChatMessage.class);
//        chatService.sendMessageToRoom(roomId, chatMessage);

        chatMessage.setMessage(chatMessage.getSender() + " 님이 퇴장 하셨습니다.");
        chatService.userLeftRoom(roomId);

        addToDatabase(roomId, chatMessage);

        // double check
        sessionManager.removeSession(accessor.getSessionId());
        messagingTemplate.convertAndSend("/topic/room/name/" + roomId, Map.of("title", chatService.getRoomUserCount(roomId)));
        messagingTemplate.convertAndSend("/topic/room/currentUsers", getCurrentUsers());

        return chatMessage;
    }

    @Scheduled(cron = "*/30 * * * * *") // sends recommended product every 30 seconds
    public void sendProductToWhole() {
        chatService.findAllRoom().stream()
                .filter(this::isEligibleForProductRecommendation)
                .forEach(this::processRoomAndStoreFuture);
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void processGPTnotice() {
        chatService.findAllRoom().stream()
                .filter(this::isEligibleForGPTNotice)
                .forEach(this::processGPTAndStoreFuture);
    }

    @Async
    public CompletableFuture<Void> processGPT(ChatRoom room) {
        try {
            var chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.NOTICE);

            // Extract the first four characters or the entire string if it's shorter than four characters
            var roomName = room.getName();
            var shortName = roomName.length() > 4 ? roomName.substring(0, 4) : roomName;

            chatMessage.setMessage("[GPT 팁] " + gptService.generateNotice(shortName));
            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), chatMessage);

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            // Handle or log the exception
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    public CompletableFuture<Void> processRoom(ChatRoom room) {
        try {
            var chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.NOTICE);

            // Extract the first four characters or the entire string if it's shorter than four characters
            var roomName = room.getName();
            var shortName = roomName.length() > 4 ? roomName.substring(0, 4) : roomName;

            var cosmetics = getCosmeticIdsByProbability(shortName);

            if (!cosmetics.isEmpty()) {
                int randomIndex = random.nextInt(cosmetics.size());
                Cosmetic randomCosmetic = cosmetics.get(randomIndex);
                chatMessage.setMessage("[공지] " + room.getName() + "! 이러한 제품은 어때요? " + randomCosmetic.getName());
                messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), chatMessage);
            }

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            // Handle or log the exception
            return CompletableFuture.failedFuture(e);
        }
    }

    private void cancelFuture(CompletableFuture<Void> future) {
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
    }

    private boolean isEligibleForProductRecommendation(ChatRoom room) {
        return !room.getName().equals("BeautyMinder") && room.getUserCounts() >= 1;
    }

    private boolean isEligibleForGPTNotice(ChatRoom room) {
        return !room.getName().equals("BeautyMinder") && room.getUserCounts() >= 1;
    }

    private void processRoomAndStoreFuture(ChatRoom room) {
        CompletableFuture<Void> future = processRoom(room);
        roomFutures.put(room.getRoomId(), future);
    }

    private void processGPTAndStoreFuture(ChatRoom room) {
        CompletableFuture<Void> future = processGPT(room);
        gptFutures.put(room.getRoomId(), future);
    }

    private List<Cosmetic> getCosmeticIdsByProbability(String baumannSkinType) {
        // Get reviews filtered by the probability scores from the NLP analysis
        List<Review> probablyBaumannReviews = reviewService.getReviewsForRecommendation(3, baumannSkinType);

        return probablyBaumannReviews.stream()
                .map(Review::getCosmetic)
                .collect(Collectors.toList());
    }

    private void addToDatabase(String roomId, ChatMessage chatMessage) {
        var roomName = chatService.findRoomById(roomId).getName();
        if (roomName.equals("BeautyMinder")) {
            var optionalChatLog = chatLogRepository.findByRoomName(roomName);
            optionalChatLog.ifPresentOrElse(
                    chatlog -> {
                        var msgFormat = ChatLog.Message.builder()
                                .type(chatMessage.getType())
                                .content(chatMessage.getMessage())
                                .sender(chatMessage.getSender())
                                .timestamp(System.currentTimeMillis()).build();
                        chatlog.addMessage(msgFormat);
                        chatLogRepository.save(chatlog);
                    },
                    () -> {
                        ChatLog newChatLog = new ChatLog();
                        newChatLog.setRoomName(roomName);
                        newChatLog.setMessages(new ArrayList<>());
                        var msgFormat = ChatLog.Message.builder()
                                .type(chatMessage.getType())
                                .content(chatMessage.getMessage())
                                .sender(chatMessage.getSender())
                                .timestamp(System.currentTimeMillis()).build();
                        newChatLog.addMessage(msgFormat);
                        chatLogRepository.save(newChatLog);
                    }
            );
        }
    }
}