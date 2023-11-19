package app.beautyminder.controller.test;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Todo;
import app.beautyminder.domain.User;
import app.beautyminder.repository.TodoRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.LogService;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/test")
public class MongoViewController {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final CosmeticService cosmeticService;
    private final LogService logService;

    @GetMapping("/spring/list")
    public String getSpringLogHTML(Model model) {
        try {
            var logs = logService.getTodaysLogs();
            var jsonLogs = logs.stream()
                    .map(log -> {
                        try {
                            Map<String, Object> logMap = objectMapper.readValue(log, new TypeReference<Map<String, Object>>() {});
                            if(logMap.containsKey("document") && ((Map)logMap.get("document")).containsKey("@timestamp")) {
                                String timestamp = (String)((Map)logMap.get("document")).get("@timestamp");
                                ZonedDateTime zdt = ZonedDateTime.parse(timestamp);
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.KOREA);
                                ((Map)logMap.get("document")).put("@timestamp", zdt.format(formatter));
                            }

                            String message = (String)((Map)logMap.get("document")).get("message");
                            Pattern pattern = Pattern.compile("(GET|POST|DELETE|PATCH|PUT)\\s(/[^\\s]+)");
                            Matcher matcher = pattern.matcher(message);
                            if (matcher.find()) {
                                String httpMethodAndEndpoint = matcher.group(0);
                                String formatted = "<b><u>" + httpMethodAndEndpoint + "</u></b>";
                                message = message.replace(httpMethodAndEndpoint, formatted);
                                ((Map)logMap.get("document")).put("message", message);
                            }

                            return logMap;
                        } catch (IOException e) {
                            return "Error parsing log entry";
                        }
                    })
                    .collect(Collectors.toList());

            model.addAttribute("logs", jsonLogs);
            return "db/logList";
        } catch (IOException e) {
            model.addAttribute("error", "Error retrieving logs");
            return "error/error"; // Replace with your error Thymeleaf template name
        }
    }

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

    @GetMapping("/cosmetic/list")
    public String listCosmetics(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "12") int size,
                                Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cosmetic> cosmeticList = cosmeticService.getAllCosmeticsInPage(pageable);
        model.addAttribute("cosmeticList", cosmeticList);
        return "db/cosmeticList";
    }
}
