package app.beautyminder.config.api;

import java.util.Set;
import java.util.regex.Pattern;

public record ApiPattern(Pattern pattern, Set<String> methods) {

    // if not specified, every method will be blocked (empty method)
    public ApiPattern(String regex, String... methods) {
        this(Pattern.compile(regex), methods.length == 0 ? Set.of() : Set.of(methods));
    }

    public boolean matches(String uri, String method) {
        return pattern.matcher(uri).matches() && (methods.isEmpty() || methods.contains(method));
    }
}
