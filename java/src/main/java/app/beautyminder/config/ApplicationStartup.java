package app.beautyminder.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class ApplicationStartup {

    @EventListener(ApplicationReadyEvent.class)
    public void logStartup() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        String startupMessage = String.format(
                "=== BEMINDER: Spring Boot Application Started === Timestamp: (%s) | Ready to Serve Requests ===",
                dtf.format(now)
        );
        log.info(startupMessage);
    }
}
