package app.beautyminder.service;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.GPTReview;
import app.beautyminder.domain.Review;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.GPTReviewRepository;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.service.cosmetic.GPTService;
import app.beautyminder.service.review.ReviewService;
import app.beautyminder.service.vision.VisionService;
import com.google.cloud.vision.v1.*;
import io.github.flashvayne.chatgpt.dto.chat.MultiChatRequest;
import io.github.flashvayne.chatgpt.service.ChatgptService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VisionServiceTest {

    private VisionService visionService;
    private ImageAnnotatorClient mockClient;

    @BeforeEach
    void setUp() {
        mockClient = mock(ImageAnnotatorClient.class);
        visionService = new VisionService();
    }

    @Test
    public void testExecuteWithUrl() throws Exception {
        // Prepare a mock response
        AnnotateImageResponse annotateImageResponse = AnnotateImageResponse.newBuilder()
                .addTextAnnotations(EntityAnnotation.newBuilder().setDescription("2019-12-13").build())
                .build();
        BatchAnnotateImagesResponse mockResponse = BatchAnnotateImagesResponse.newBuilder()
                .addResponses(annotateImageResponse)
                .build();

        // Mock the behavior of the client
        when(mockClient.batchAnnotateImages(any(BatchAnnotateImagesRequest.class))).thenReturn(mockResponse);

        // Execute the method under test
        Optional<String> result = visionService.execute("https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAxNzExMDhfNjEg%2FMDAxNTEwMTA0NDE5NDA1.8BER8TU9fnZECZ09BjXeY2Khyyucxmys6awwVYlLmvQg.nFdrDoY-MxVB0zUQGSQOTSmRK1qLa5zxJ36YRtFOkqEg.JPEG.laykor%2FKakaoTalk_20171108_095704064.jpg&type=sc960_832");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("2019-12-13", result.get());
    }

    @Test
    public void testExecuteWithBase64EncodedString() throws Exception {
        // Mock response setup as before
        AnnotateImageResponse annotateImageResponse = AnnotateImageResponse.newBuilder()
                .addTextAnnotations(EntityAnnotation.newBuilder().setDescription("2025-10-06").build())
                .build();
        BatchAnnotateImagesResponse mockResponse = BatchAnnotateImagesResponse.newBuilder()
                .addResponses(annotateImageResponse)
                .build();

        when(mockClient.batchAnnotateImages(any(BatchAnnotateImagesRequest.class))).thenReturn(mockResponse);

        // Execute the method under test
        File imageFile = new File("src/test/resources/ocr.jpg"); // Ensure file path is correct
        MockMultipartFile image = new MockMultipartFile("image",
                "ocr.jpg",
                "image/jpeg",
                Files.readAllBytes(imageFile.toPath()));
        String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
        Optional<String> result = visionService.execute(base64Image);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("2025-10-06", result.get());
    }

    @Test
    public void testExecuteWithException() throws Exception {
        // Setup to throw an exception
        when(mockClient.batchAnnotateImages(any(BatchAnnotateImagesRequest.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Execute the method under test
        Optional<String> result = visionService.execute("https://example.com/image.jpg");

        // Assertions
        assertFalse(result.isPresent());
    }

}