package app.beautyminder.config.api;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ApiPatternService {

    private final List<ApiPattern> apiPatterns = new ArrayList<>();

    public ApiPatternService(ApiConfig apiConfig) {
        apiConfig.getPatterns().forEach(p ->
                apiPatterns.add(new ApiPattern(p.getRegex(), p.getMethods().toArray(new String[0])))
        );
    }

    // Method to check if a request matches any pattern
    public boolean isRequestProtected(String uri, String method) {
        return apiPatterns.stream()
                .anyMatch(pattern -> pattern.matches(uri, method));
    }
}
