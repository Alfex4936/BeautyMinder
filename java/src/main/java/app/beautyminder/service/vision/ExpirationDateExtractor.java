package app.beautyminder.service.vision;


import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ExpirationDateExtractor {

    // First pattern for perfect matches
    private static final String[] PERFECT_DATE_PATTERN = {
            "EXP\\s*(\\d{4})[\\s.-]*(\\d{2})[\\s.-]*(\\d{2})",
            "(\\d{4})[\\s.-]*(\\d{2})[\\s.-]*(\\d{2})\\s*까지",
            "사용기한\\s*(\\d{4})[\\s.-]*(\\d{2})[\\s.-]*(\\d{2})",
            "(\\d{4})(\\d{2})(\\d{2})\\D",
            "(\\d{4})[.-/](\\d{2})[.-/](\\d{2})",
            "EXP\\s*(\\d{2})[.-/](\\d{2})[.-/](\\d{2})",
            "([0-9O]{4})([0-9O]{2})([0-9O]{2})\\D"}; // 'D' for non-digit character

    // Second pattern for common OCR errors
    private static final String[] OCR_ERROR_DATE_PATTERN = {"EXP\\s*([0-9O]{4})[\\s.-]*([0-9O]{2})[\\s.-]*([0-9O]{2})" // 'O' can be misread as '0'
            , "([0-9O]{4})[\\s.-]*([0-9O]{2})[\\s.-]*([0-9O]{2})\\s*까지" // including '까지' for context
            , "사용기한\\s*([0-9O]{4})[\\s.-]*([0-9O]{2})[\\s.-]*([0-9O]{2})" // including '사용기한' for context
            , "([0-9O]{4})([0-9O]{2})([0-9O]{2})\\D" // 'D' for non-digit character
            , "([0-9O]{4})[.-/]([0-9O]{2})[.-/]([0-9O]{2})" // slashes and dashes for date separation
            , "EXP\\s*([0-9O]{2})[.-/]([0-9O]{2})[.-/]([0-9O]{2})" // 'EXP' for expiration context
            , "EXP\\s*([0-9O]{5,})[\\s.-]*([0-9O]{2})[\\s.-]*([0-9O]{2})"
            , "([0-9O]{5})[\\s.-]*([0-9O]{2})[\\s.-]*([0-9O]{2})"
            , "([0-9O]{4,5})[\\s.-]*([0-9O]{2})[\\s.-]*([0-9O]{2})"
            , "E?XP\\s*([0-9]{5,})[\\s.-]*([0-9]{2})[\\s.-]*([0-9]{2})"
            , "(20\\d{2})\\s*(\\d{2})\\s*(\\d{2})\\s*\\d*"
            , "\\b(20[0-9O]{2})[\\s.-]*([0-9O]{2})[\\s.-]*([0-9O]{2})\\d+\\b"
            , "\\b(20\\d{2})(\\d{2})(\\d{2})\\d*\\b"};

    public static Optional<String> extractExpirationDate(String text) {
        // Use Optional to encapsulate nullable latestDate
        Optional<LocalDate> latestDate = findDatesWithPattern(text, PERFECT_DATE_PATTERN);

        // If no perfect matches found, try with OCR errors
        if (latestDate.isEmpty()) {
            latestDate = findDatesWithPattern(text, OCR_ERROR_DATE_PATTERN);
        }

        // Convert Optional<LocalDate> to Optional<String>
        return latestDate.map(LocalDate::toString);
    }

    private static Optional<LocalDate> findDatesWithPattern(String text, String[] patterns) {
        return Arrays.stream(patterns)
                .flatMap(pattern -> Pattern.compile(pattern)
                        .matcher(text)
                        .results()) // Flattens Stream<Stream<MatchResult>> to Stream<MatchResult>
                .map(ExpirationDateExtractor::matchDate) // Map each MatchResult to Optional<LocalDate>
                .flatMap(Optional::stream) // Convert Optional<LocalDate> to Stream<LocalDate>
                .max(Comparator.naturalOrder()); // Get the maximum date
    }

    private static Optional<LocalDate> matchDate(MatchResult match) {
        if (Objects.isNull(match.group(1)) || Objects.isNull(match.group(2)) || Objects.isNull(match.group(3))) {
            return Optional.empty();
        }

        var year = normalizeYear(Optional.ofNullable(match.group(1)).orElse(""));
        var month = Optional.ofNullable(match.group(2)).orElse("").replace("O", "0");
        var day = Optional.ofNullable(match.group(3)).orElse("").replace("O", "0");

        if (year.length() == 2) {
            year = "20" + year;
        }

        try {
            var date = LocalDate.parse(String.format("%s-%s-%s", year, month, day));
            return Optional.of(date);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String normalizeYear(String year) {
        // Replace common OCR misreads for digits
        year = year.replace("O", "0").replace("I", "1").replace("S", "5");

        // Handle cases where the year has an extra digit
        if (year.length() == 5 && year.startsWith("20")) {
            // Check if the third digit (index 2) is the extra one
            if (year.charAt(2) != '2' && year.charAt(2) != '3') {
                year = year.substring(0, 2) + year.substring(3);
            }
            // Check if the fourth digit (index 3) is the extra one
            else if (year.charAt(3) != '0') {
                year = year.substring(0, 3) + year.substring(4);
            }
            // Otherwise, assume the extra digit is at the end
            else {
                year = year.substring(0, 4);
            }
        }

        return year;
    }

}