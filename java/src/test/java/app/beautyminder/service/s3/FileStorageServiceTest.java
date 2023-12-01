package app.beautyminder.service.s3;

import app.beautyminder.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class FileStorageServiceTest {

    private final String folderPath = "profle/";
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private FileStorageService fileStorageService;
    private String fileUrl;
    private String thumbnailUrl;

    @BeforeAll
    public void setUp() {

    }

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    public void testStoreFile_Success() throws IOException {
        File imageFile = new File("src/test/resources/ajou.png"); // Ensure file path is correct
        MockMultipartFile image1 = new MockMultipartFile("images",
                "ajou.png",
                "image/png",
                Files.readAllBytes(imageFile.toPath()));

        // Test storing file
        fileUrl = fileStorageService.storeFile(image1, folderPath, false);
        assertNotNull(fileUrl);
        assertTrue(fileUrl.contains(".png")); // UUID name
    }

    @Test
    public void testStoreFile_InvalidFileType() {
        // Mock a file with an invalid content type
        MultipartFile invalidFile = new MockMultipartFile("test.png", "test.png", "image/abc", "test data".getBytes());

        // Test storing file
        // FileStorageException class is private
        Assertions.assertThrows(RuntimeException.class, () -> fileStorageService.storeFile(invalidFile, folderPath, false));
    }

    @Test
    public void testLoadFile_Success() throws IOException {
        // Test storing file
        var resource = fileStorageService.loadFile(fileUrl);

        assertNotNull(resource);
    }

    @Test
    public void testLoadFile_NotFound() throws IOException {
        // Test storing file
        Assertions.assertThrows(RuntimeException.class, () -> fileStorageService.loadFile("HAHAHAHA"));
    }

    @Test
    public void testUploadThumbnail_Success() throws IOException {
        File imageFile = new File("src/test/resources/ajou.png"); // Ensure file path is correct
        MockMultipartFile image1 = new MockMultipartFile("images",
                "ajou.png",
                "image/png",
                Files.readAllBytes(imageFile.toPath()));


        thumbnailUrl = fileStorageService.uploadThumbnail(image1, "ajou.png");
        assertTrue(thumbnailUrl.contains("150x150"));
    }


    @Test
    public void testDeleteFile_Success() throws IOException {
        fileStorageService.deleteFile(fileUrl);
    }

    @AfterEach
    public void cleanUp() {
        // Clea  up logic to run after each test if needed
    }

    @AfterAll
    public void cleanUpAll() {
        fileStorageService.deleteFile(fileUrl);
        fileStorageService.deleteFile(thumbnailUrl);
    }
}
