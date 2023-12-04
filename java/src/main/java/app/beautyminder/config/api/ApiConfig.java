package app.beautyminder.config.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Configuration
@ConfigurationProperties(prefix = "api-config")
public class ApiConfig {

    private final List<ApiPatternProperties> patterns = new ArrayList<>();

    @Getter
    @Setter
    public static class ApiPatternProperties {
        private String regex;
        private Set<String> methods = new HashSet<>();
    }
}
