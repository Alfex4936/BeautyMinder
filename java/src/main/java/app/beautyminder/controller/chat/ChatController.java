package app.beautyminder.controller.chat;

import app.beautyminder.domain.User;
import app.beautyminder.dto.chat.ChatRoom;
import app.beautyminder.service.chat.ChatService;
import app.beautyminder.util.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;


    @GetMapping("/list")
    public String listChat(Model model) {
        List<ChatRoom> roomList = chatService.findAllRoom();
        model.addAttribute("roomList", roomList);
        return "chat/chatList";
    }


    @PostMapping("/create")
    public String createRoom(Model model, @RequestParam String name, String username) {
        ChatRoom room = chatService.createRoom(name);
        model.addAttribute("room", room);
        model.addAttribute("username", username);
        return "chat/chatRoom";
    }

//    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/enter")
    public String enterRoom(Model model, @RequestParam String roomId) {
        ChatRoom room = chatService.findRoomById(roomId);
        model.addAttribute("room", room);
        return "chat/chatRoom";
    }

}