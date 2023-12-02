package app.beautyminder.controller.vision;

import app.beautyminder.controller.ocr.VisionController;
import app.beautyminder.service.vision.VisionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ActiveProfiles({"awsBasic", "test"})
public class VisionApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VisionService visionService;

    @InjectMocks
    private VisionController visionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(visionController).build();
    }

    @Test
    void testParseImageByGoogleVision_withImageFile() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "filename.txt", "text/plain", "some xml".getBytes());

        when(visionService.execute(anyString())).thenReturn(Optional.of("someDate"));

        mockMvc.perform(multipart("/vision/ocr").file(image))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data").value("someDate"));

        verify(visionService, times(1)).execute(anyString());
    }

    @Test
    void testParseImageByGoogleVision_withUrl() throws Exception {
        String imageUrl = "http://example.com/image.jpg";
        when(visionService.execute(imageUrl)).thenReturn(Optional.of("someDate"));

        mockMvc.perform(post("/vision/ocr").param("url", imageUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("someDate"));

        verify(visionService, times(1)).execute(imageUrl);
    }

    @Test
    void testParseImageByGoogleVision_noImageOrUrl() throws Exception {
        mockMvc.perform(post("/vision/ocr"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No image or URL provided"));
    }

    @Test
    void testParseImageByGoogleVision_serviceReturnsEmpty() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "filename.txt", "text/plain", "some xml".getBytes());

        when(visionService.execute(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(multipart("/vision/ocr").file(image))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No expiration date found or error occurred"));
    }

    @Test
    void testParseImageByGoogleVision_ioExceptionDuringBase64Encoding() throws Exception {
        // Create a spy on an actual MultipartFile
        MockMultipartFile image = new MockMultipartFile("image", "filename.txt", "text/plain", "some xml".getBytes());
        MockMultipartFile spyImage = spy(image);

        // Force the spy to throw an IOException when getBytes() is called
        doThrow(new IOException()).when(spyImage).getBytes();

        mockMvc.perform(multipart("/vision/ocr").file(spyImage))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error processing the image file"));
    }

}