package app.beautyminder.service.vision;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class OCRExpirationDateTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    public void extractExpirationDate_NoPattern() {
        String textWithDate = "1234-5678-90";

        Optional<String> extractedDate = ExpirationDateExtractor.extractExpirationDate(textWithDate);

        assertTrue(extractedDate.isEmpty());
    }

    @Test
    public void extractExpirationDate_ShouldExtractDateForPerfectPatterns() {
        String textWithDate = "EXP 2023-05-20"; // Use various strings that match PERFECT_DATE_PATTERN

        Optional<String> extractedDate = ExpirationDateExtractor.extractExpirationDate(textWithDate);

        assertTrue(extractedDate.isPresent());
        assertEquals("2023-05-20", extractedDate.get());
    }

    @Test
    public void extractExpirationDate_ShouldExtractDateForOCRErrorPatterns() {
        String textWithOCRError = "EXP 2O23-O5-2O"; // Use various strings that match OCR_ERROR_DATE_PATTERN

        Optional<String> extractedDate = ExpirationDateExtractor.extractExpirationDate(textWithOCRError);

        assertTrue(extractedDate.isPresent());
        assertEquals("2023-05-20", extractedDate.get());
    }

    @Test
    public void extractExpirationDate_ShouldReturnEmptyForNonMatchingStrings() {
        String textWithoutDate = "No date here";

        Optional<String> extractedDate = ExpirationDateExtractor.extractExpirationDate(textWithoutDate);

        assertFalse(extractedDate.isPresent());
    }

    @Test
    public void extractExpirationDate_ShouldHandleEdgeCases() {
        String textWithEdgeCase = "EXP 20235-05-20"; // A string with an edge case

        Optional<String> extractedDate = ExpirationDateExtractor.extractExpirationDate(textWithEdgeCase);

        assertTrue(extractedDate.isPresent());
        assertEquals("2025-05-20", extractedDate.get());
    }
    @Test
    public void extractYear_ShouldExtractYearWhenPresent() {
        String textWithYear = "Product made in 2023";
        Optional<String> extractedYear = ExpirationDateExtractor.extractYear(textWithYear);
        assertTrue(extractedYear.isPresent());
        assertEquals("2023", extractedYear.get());
    }

    @Test
    public void extractYear_ShouldReturnEmptyWhenYearNotPresent() {
        String textWithoutYear = "Product made in May";
        Optional<String> extractedYear = ExpirationDateExtractor.extractYear(textWithoutYear);
        assertFalse(extractedYear.isPresent());
    }

    @Test
    public void extractMonth_ShouldExtractMonthWhenPresent() {
        String textWithMonth = "Best before 05";
        Optional<String> extractedMonth = ExpirationDateExtractor.extractMonth(textWithMonth);
        assertTrue(extractedMonth.isPresent());
        assertEquals("05", extractedMonth.get());
    }

    @Test
    public void extractMonth_ShouldReturnEmptyWhenMonthNotPresent() {
        String textWithoutMonth = "Product made in 2023";
        Optional<String> extractedMonth = ExpirationDateExtractor.extractMonth(textWithoutMonth);
        assertFalse(extractedMonth.isPresent());
    }

    @Test
    public void guessDateBasedOnPartialInfo_ShouldReturnDateForYearOnly() {
        String textWithYear = "EXP Year: 2023";
        LocalDate currentDate = LocalDate.now();
        Optional<LocalDate> guessedDate = ExpirationDateExtractor.guessDateBasedOnPartialInfo(textWithYear);
        assertTrue(guessedDate.isPresent());
        assertEquals(LocalDate.of(2023, currentDate.getMonthValue(), currentDate.getDayOfMonth()), guessedDate.get());
    }

    @Test
    public void guessDateBasedOnPartialInfo_ShouldReturnDateForMonthOnly() {
        String textWithMonth = "EXP Month: 05";
        LocalDate currentDate = LocalDate.now();
        LocalDate lastDayOfMonth = currentDate.withMonth(5).withDayOfMonth(currentDate.getMonth().length(currentDate.isLeapYear()));
        Optional<LocalDate> guessedDate = ExpirationDateExtractor.guessDateBasedOnPartialInfo(textWithMonth);
        assertTrue(guessedDate.isPresent());
        assertEquals(LocalDate.of(currentDate.getYear(), 5, lastDayOfMonth.getDayOfMonth()), guessedDate.get());
    }

    @Test
    public void guessDateBasedOnPartialInfo_ShouldReturnEmptyForNoYearOrMonth() {
        String textWithoutYearOrMonth = "No date information";
        Optional<LocalDate> guessedDate = ExpirationDateExtractor.guessDateBasedOnPartialInfo(textWithoutYearOrMonth);
        assertFalse(guessedDate.isPresent());
    }

    @AfterEach
    public void cleanUp() {
        // Clean-up logic to run after each test if needed
    }

    @AfterAll
    public void cleanUpAll() {

    }
}
