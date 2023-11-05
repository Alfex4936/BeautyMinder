package app.beautyminder.controller.elasticsearch;

import app.beautyminder.service.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

// TODO: change all to ADMIN_ROLE
@RequiredArgsConstructor
@RestController
@RequestMapping("/log")
public class LogController {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final LogService logService;

    @Operation(
            summary = "Watch Spring Boot logs",
            description = "Spring Boot 서버 로그 보기 (from Elasticsearch, UTC time)",
            tags = {"Log Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = Object.class, type="array")))
            }
    )
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

    @Operation(
            summary = "Delete Spring Boot logs",
            description = "Spring Boot 서버 로그 삭제 (from Elasticsearch, UTC time)",
            tags = {"Log Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = Object.class, type="array")))
            }
    )
    @DeleteMapping("/spring/delete")
    public ResponseEntity<String> dropLogDocuments() {
        String todayDate = LocalDate.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("YYYY.MM.dd"));
        String todayIndex = "logstash-logs-" + todayDate;

        logService.deleteAllDocuments(todayIndex);
        return ResponseEntity.ok("Deleted log indices successfully");
    }
}
