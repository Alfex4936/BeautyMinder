package app.beautyminder.service.vision;


import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpirationDateExtractor {

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "EXP\\s*(\\d{4})[\\s.-]*(\\d{2})[\\s.-]*(\\d{2})|"
                    + "(\\d{4})[\\s.-]*(\\d{2})[\\s.-]*(\\d{2})\\s*까지|"
                    + "사용기한\\s*(\\d{4})[\\s.-]*(\\d{2})[\\s.-]*(\\d{2})|"
                    + "(\\d{4})(\\d{2})(\\d{2})\\D|"
                    + "(\\d{4})[.-/](\\d{2})[.-/](\\d{2})|"
                    + "EXP\\s*(\\d{2})[.-/](\\d{2})[.-/](\\d{2})"
    );

    public static Optional<String> extractExpirationDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i += 3) {
                if (matcher.group(i) != null) {
                    String year = matcher.group(i);
                    String month = matcher.group(i + 1);
                    String day = matcher.group(i + 2);
                    return Optional.of(String.format("%s-%s-%s", year, month, day));
                }
            }
        }
        return Optional.empty();
    }
}