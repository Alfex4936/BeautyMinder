package app.beautyminder.controller.test;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Todo;
import app.beautyminder.domain.User;
import app.beautyminder.repository.TodoRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.LogService;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticService;
import app.beautyminder.service.vision.VisionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
    private final VisionService visionService;

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

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/ocr")
    public String testOCR(Model model) {
        return "db/ocrTest";
    }

    @PostMapping("/ocr/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        var base64Image = Base64.getEncoder().encodeToString(file.getBytes());

        // Save the file to a temporary directory and create a reference to it
        String filename = file.getOriginalFilename();
        Path tempFile = Files.createTempFile("upload_", filename);
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        // Create a URL to access the temporary file
        String tempFileUrl = "/test/temp-files/" + tempFile.getFileName().toString();

        // Store the URL in the redirect attributes
        redirectAttributes.addFlashAttribute("tempFileUrl", tempFileUrl);

        // Process the file and call the OCR API
        visionService.execute(base64Image).ifPresentOrElse(
                result -> {
                    redirectAttributes.addFlashAttribute("result", result);
                },
                () -> {
                    redirectAttributes.addFlashAttribute("result", "");
                }
        );

        return "redirect:/test/ocr";
    }

    @GetMapping("/temp-files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveTempFile(@PathVariable String filename) throws MalformedURLException {
        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir")).resolve(filename);
        if (Files.exists(tempFile)) {
            Resource file = new UrlResource(tempFile.toUri());
            return ResponseEntity.ok().body(file);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}
