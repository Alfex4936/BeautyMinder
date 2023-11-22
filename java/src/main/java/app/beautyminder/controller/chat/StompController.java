package app.beautyminder.controller.chat;

import app.beautyminder.domain.ChatLog;
import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.dto.chat.ChatMessage;
import app.beautyminder.dto.chat.ChatRoom;
import app.beautyminder.repository.ChatLogRepository;
import app.beautyminder.service.chat.ChatService;
import app.beautyminder.service.review.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vane.badwordfiltering.BadWordFiltering;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
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
    private final ChatLogRepository chatLogRepository;

    private final Random random = new Random();
    private final BadWordFiltering badWordFiltering = new BadWordFiltering();

    private final Map<String, CompletableFuture<Void>> roomFutures = new ConcurrentHashMap<>();

    @PostConstruct
    public void addSomeWords() {
        var lists = List.of("10새", "10새기", "10새리", "10세리", "10쉐이", "10쉑", "10스", "10쌔",
                "10쌔기", "10쎄", "10알", "10창", "10탱", "18것", "18넘", "18년", "18노", "18놈",
                "18뇬", "18럼", "18롬", "18새", "18새끼", "18색", "18세끼", "18세리", "18섹", "18쉑", "18스", "18아",
                "c파", "c팔", "fuck", "sibal", "motherfucker", "fucker", "sex", "vagina",
                "ㄱㅐ", "ㄲㅏ", "ㄲㅑ", "ㄲㅣ", "ㅅㅂㄹㅁ", "ㅅㅐ", "ㅆㅂㄹㅁ", "ㅆㅍ", "ㅆㅣ", "ㅆ앙", "ㅍㅏ", "凸",
                "갈보", "갈보년", "강아지", "같은년", "같은뇬", "개같은", "개구라", "개년", "개놈",
                "개뇬", "개대중", "개독", "개돼중", "개랄", "개보지", "개뻥", "개뿔", "개새", "개새기", "개새끼",
                "개새키", "개색기", "개색끼", "개색키", "개색히", "개섀끼", "개세", "개세끼", "개세이", "개소리", "개쑈",
                "개쇳기", "개수작", "개쉐", "개쉐리", "개쉐이", "개쉑", "개쉽", "개스끼", "개시키", "개십새기",
                "개십새끼", "개쐑", "개씹", "개아들", "개자슥", "개자지", "개접", "개좆", "개좌식", "개허접", "걔새",
                "걔수작", "걔시끼", "걔시키", "걔썌", "걸레", "게색기", "게색끼", "광뇬", "구녕", "구라", "구멍",
                "그년", "그새끼", "냄비", "놈현", "뇬", "눈깔", "뉘미럴", "니귀미", "니기미", "니미", "니미랄", "니미럴",
                "니미씹", "니아배", "니아베", "니아비", "니어매", "니어메", "니어미", "닝기리", "닝기미", "대가리",
                "뎡신", "도라이", "돈놈", "돌아이", "돌은놈", "되질래", "뒈져", "뒈져라", "뒈진", "뒈진다", "뒈질",
                "뒤질래", "등신", "디져라", "디진다", "디질래", "딩시", "따식", "때놈", "또라이", "똘아이", "똘아이",
                "뙈놈", "뙤놈", "뙨넘", "뙨놈", "뚜쟁", "띠바", "띠발", "띠불", "띠팔", "메친넘", "메친놈", "미췬",
                "미췬", "미친", "미친넘", "미친년", "미친놈", "미친새끼", "미친스까이", "미틴", "미틴넘", "미틴년",
                "미틴놈", "바랄년", "병자", "뱅마", "뱅신", "벼엉신", "병쉰", "병신", "부랄", "부럴", "불알", "불할", "붕가",
                "붙어먹", "뷰웅", "븅", "븅신", "빌어먹", "빙시", "빙신", "빠가", "빠구리", "빠굴", "빠큐", "뻐큐",
                "뻑큐", "뽁큐", "상넘이", "상놈을", "상놈의", "상놈이", "새갸", "새꺄", "새끼", "새새끼", "새키",
                "색끼", "생쑈", "세갸", "세꺄", "세끼", "섹스", "쇼하네", "쉐", "쉐기", "쉐끼", "쉐리", "쉐에기",
                "쉐키", "쉑", "쉣", "쉨", "쉬발", "쉬밸", "쉬벌", "쉬뻘", "쉬펄", "쉽알", "스패킹", "스팽", "시궁창", "시끼",
                "시댕", "시뎅", "시랄", "시발", "시벌", "시바", "쉬바", "씌발", "줫", "시부랄", "시부럴", "시부리", "시불", "시브랄", "시팍",
                "시팔", "시펄", "신발끈", "심발끈", "심탱", "십8", "십라", "십새", "십새끼", "십세", "십쉐", "십쉐이", "십스키",
                "십쌔", "십창", "십탱", "싶알", "싸가지", "싹아지", "쌉년", "쌍넘", "쌍년", "쌍놈", "쌍뇬", "쌔끼",
                "쌕", "쌩쑈", "쌴년", "썅", "썅년", "썅놈", "썡쇼", "써벌", "썩을년", "썩을놈", "쎄꺄", "쎄엑",
                "쒸벌", "쒸뻘", "쒸팔", "쒸펄", "쓰바", "쓰박", "쓰발", "쓰벌", "쓰팔", "씁새", "씁얼", "씌파", "씨8",
                "씨끼", "씨댕", "씨뎅", "씨바", "씨바랄", "씨박", "씨발", "씨방", "씨방새", "씨방세", "씨밸", "씨뱅",
                "씨벌", "씨벨", "씨봉", "씨봉알", "씨부랄", "씨부럴", "씨부렁", "씨부리", "씨불", "씨붕", "씨브랄",
                "씨빠", "씨빨", "씨뽀랄", "씨앙", "씨파", "씨팍", "씨팔", "씨펄", "씸년", "씸뇬", "씸새끼", "씹같", "씹년",
                "씹뇬", "씹보지", "씹새", "씹새기", "씹새끼", "씹새리", "씹세", "씹쉐", "씹스키", "씹쌔", "씹이", "씹자지",
                "씹질", "씹창", "씹탱", "씹퇭", "씹팔", "씹할", "씹헐", "아가리", "아갈", "아갈이", "아갈통",
                "아구창", "아구통", "아굴", "얌마", "양넘", "양년", "양놈", "엄창", "엠병", "여물통", "염병", "엿같", "옘병",
                "옘빙", "오입", "왜년", "왜놈", "욤병", "육갑", "은년", "을년", "이년", "이새끼", "이새키", "이스끼",
                "이스키", "임마", "자슥", "잡것", "잡넘", "잡년", "잡놈", "저년", "저새끼", "접년", "젖밥", "조까",
                "조까치", "조낸", "조또", "조랭", "조빠", "조쟁이", "조지냐", "조진다", "조찐", "조질래", "존나", "존나게", "존니", "존만",
                "존만한", "좀물", "좁년", "좁밥", "좃까", "좃또", "좃만", "좃밥", "좃이", "좃찐", "좆같", "좆까", "좆나",
                "좆또", "좆만", "좆밥", "좆이", "좆찐", "좇같", "좇이", "좌식", "주글", "주글래", "주데이", "주뎅",
                "주뎅이", "주둥아리", "주둥이", "주접", "주접떨", "죽고잡", "죽을래", "죽통", "쥐랄", "쥐롤",
                "쥬디", "지랄", "지럴", "지롤", "지미랄", "짜식", "짜아식", "쪼다", "쫍빱", "찌랄", "창녀", "캐년",
                "캐놈", "캐스끼", "캐스키", "캐시키", "탱구", "팔럼", "퍽큐", "호로", "호로놈", "호로새끼",
                "호로색", "호로쉑", "호로스까이", "호로스키", "후라들", "후래자식", "후레", "후뢰", "ㅄ");
        badWordFiltering.addAll(lists);
        badWordFiltering.remove("공지");
    }

//    @org.springframework.context.event.EventListener
//    public void handleSessionDisconnected(SessionDisconnectEvent event) {
//        log.info("BEMINDER: {}", event.toString());
//        String roomId = event.getSessionId();
//
//        chatService.userLeftRoom(roomId);
//
//        if (chatService.getRoomUserCount(roomId) == 0) {
//            CompletableFuture<Void> future = roomFutures.remove(roomId);
//            if (future != null && !future.isDone()) {
//                future.cancel(true); // Attempt to cancel the ongoing task
//            }
//        }
//    }


    @PreDestroy
    public void onDestroy() {
        roomFutures.values().forEach(future -> {
            if (future != null && !future.isDone()) {
                future.cancel(true);
            }
        });
        roomFutures.clear();
    }

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

        // Optionally, broadcast that a user has entered the room
//        chatService.sendMessageToRoom(roomId, chatMessage);
        chatService.userEnteredRoom(roomId);

        addToDatabase(roomId, chatMessage);

        messagingTemplate.convertAndSend("/topic/room/name/" + roomId, Map.of("title", chatService.getRoomUserCount(roomId)));

//        messagingTemplate.convertAndSend("/topic/room/" + roomId, chatMessage);

        return chatMessage;
    }

    @MessageMapping("/chat.quit/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage quitRoom(@DestinationVariable String roomId, @Payload String messageJson) throws Exception {
        ChatMessage chatMessage = objectMapper.readValue(messageJson, ChatMessage.class);
//        chatService.sendMessageToRoom(roomId, chatMessage);

        chatMessage.setMessage(chatMessage.getSender() + " 님이 퇴장 하셨습니다.");
        chatService.userLeftRoom(roomId);

        addToDatabase(roomId, chatMessage);

        messagingTemplate.convertAndSend("/topic/room/name/" + roomId, Map.of("title", chatService.getRoomUserCount(roomId)));

        return chatMessage;
    }

    @Scheduled(cron = "*/45 * * * * *") // sends recommended product every 15 seconds
    public void sendProductToWhole() {

        // stream api
        chatService.findAllRoom().stream()
                .filter(room -> !room.getName().equals("BeautyMinder"))
                .filter(room -> room.getUserCounts() >= 1)
                .forEach(room -> {
                    CompletableFuture<Void> future = processRoom(room);
                    roomFutures.put(room.getRoomId(), future);
                });

        // Wait for all futures to be done
        CompletableFuture.allOf(roomFutures.values().toArray(new CompletableFuture[0])).join();
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