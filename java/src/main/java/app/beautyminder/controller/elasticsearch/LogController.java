package app.beautyminder.controller.elasticsearch;

import app.beautyminder.dto.CosmeticMetricData;
import app.beautyminder.service.LogService;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import app.beautyminder.service.cosmetic.CosmeticSearchService;
import app.beautyminder.service.cosmetic.ReviewSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

// TODO: change all to ADMIN_ROLE
@RequiredArgsConstructor
@RestController
@RequestMapping("/log")
public class LogController {

    private final LogService logService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/spring")
    public ResponseEntity<List<Object>> getSpringLog() {
        try {
            List<String> logs = logService.getTodaysLogs();

            // Convert the List of JSON strings to a List of Maps (or any object structure you want)
            List<Object> jsonLogs = logs.stream()
                    .map(log -> {
                        try {
                            return objectMapper.readValue(log, Object.class);
                        } catch (IOException e) {
                            // Log error and possibly return a placeholder or skip this entry
                            return "Error parsing log entry";
                        }
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(jsonLogs);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/spring/delete")
    public ResponseEntity<String> dropReviewDocuments() {
        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("YYYY.MM.dd"));
        String todayIndex = "logstash-logs-" + todayDate;

        logService.deleteAllDocuments(todayIndex);
        return ResponseEntity.ok("Deleted review indices successfully");
    }
}
