package app.beautyminder.controller.test;

import app.beautyminder.domain.User;
import app.beautyminder.dto.chat.ChatRoom;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/test") // Base path for all routes in this controller
public class UserViewController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/user/list")
    public String listUser(Model model) {
        List<User> userList = userRepository.findAll();
        model.addAttribute("userList", userList);
        return "db/userList";
    }
}
