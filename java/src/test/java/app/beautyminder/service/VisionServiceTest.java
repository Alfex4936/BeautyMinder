package app.beautyminder.service;

import app.beautyminder.service.vision.VisionService;
import com.google.cloud.vision.v1.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public void testExecuteWithUrl() {
        // Prepare a mock response
        AnnotateImageResponse annotateImageResponse = AnnotateImageResponse.newBuilder()
                .addTextAnnotations(EntityAnnotation.newBuilder().setDescription("2020-06-27").build())
                .build();
        BatchAnnotateImagesResponse mockResponse = BatchAnnotateImagesResponse.newBuilder()
                .addResponses(annotateImageResponse)
                .build();

        // Mock the behavior of the client
        when(mockClient.batchAnnotateImages(any(BatchAnnotateImagesRequest.class))).thenReturn(mockResponse);

        // Execute the method under test
        Optional<String> result = visionService.execute("https://beautyminder-sw.s3.ap-northeast-2.amazonaws.com/test/%ED%85%8C%EC%8A%A4%ED%8A%B83.jpg");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("2020-06-27", result.get());
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
    public void testExecuteWithException() {
        // Setup to throw an exception
        when(mockClient.batchAnnotateImages(any(BatchAnnotateImagesRequest.class)))
                .thenThrow(new RuntimeException("Test exception"));

        // Execute the method under test
        Optional<String> result = visionService.execute("https://example.com/image.jpg");

        // Assertions
        assertFalse(result.isPresent());
    }

}