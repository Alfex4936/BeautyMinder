package app.beautyminder.controller.chat;

import app.beautyminder.domain.User;
import app.beautyminder.dto.chat.ChatRoom;
import app.beautyminder.service.chat.ChatService;
import app.beautyminder.util.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/login")
    public String login(Model model) {
        return "chat/login";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    // @Parameter(hidden = true) @AuthenticatedUser User user
    @GetMapping("/list")
    public String listChat(Model model, @Parameter(hidden = true) @AuthenticatedUser User user) {
        List<ChatRoom> roomList = chatService.findAllRoom();
        model.addAttribute("roomList", roomList);
        if (user.getBaumann() != null) {
            model.addAttribute("user", user);
        }
        return "chat/chatList";
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/enter")
    public String enterRoom(Model model, @RequestParam String roomId, RedirectAttributes redirectAttributes, @Parameter(hidden = true) @AuthenticatedUser User user) {
        ChatRoom room = chatService.findRoomById(roomId);
        if (room == null) {
            redirectAttributes.addFlashAttribute("error", "Room not found");
            return "redirect:/chat/list";
        }

        // block if a user has different baumann for a room
//        var roomName = room.getName();
//        var shortName = roomName.length() > 4 ? roomName.substring(0, 4) : roomName;
//
//        if (user.getBaumann() != null && !shortName.equals(user.getBaumann())) {
//            redirectAttributes.addFlashAttribute("error", "Access denied to the " + shortName + " room due to Baumann type mismatch (you " + user.getBaumann() + ")");
//            return "redirect:/chat/list";
//        }

        model.addAttribute("user", user);
        model.addAttribute("room", room);
        return "chat/chatRoom";
    }

}