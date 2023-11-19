package app.beautyminder.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationStartup {
    @EventListener(ApplicationReadyEvent.class)
    public void logStartup() {
        log.info("BEMINDER: Spring Boot application is fully started and ready to serve requests.");
    }
}
