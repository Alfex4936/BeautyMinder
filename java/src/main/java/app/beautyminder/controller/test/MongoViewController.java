package app.beautyminder.controller.test;

import app.beautyminder.domain.Todo;
import app.beautyminder.domain.User;
import app.beautyminder.repository.TodoRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/test")
public class MongoViewController {

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;

    @GetMapping("/user/list")
    public String listUser(Model model) {
        List<User> userList = userRepository.findAll();
        model.addAttribute("userList", userList);
        return "db/userList";
    }

    @GetMapping("/todo/list")
    public String listTodo(Model model) {
        List<Todo> todoList = todoRepository.findAll();
        model.addAttribute("todoList", todoList);
        return "db/todoList";
    }
}
