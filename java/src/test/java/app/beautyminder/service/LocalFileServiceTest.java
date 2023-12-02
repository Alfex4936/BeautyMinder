package app.beautyminder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class LocalFileServiceTest {

    private ResourceLoader resourceLoader;
    private ObjectMapper objectMapper;
    private LocalFileService localFileService;


    @BeforeEach
    void setUp() {
        resourceLoader = mock(ResourceLoader.class);
        objectMapper = new ObjectMapper();
        localFileService = new LocalFileService(resourceLoader, objectMapper);
    }

    @Test
    public void testReadJsonFile() throws IOException {
        String testJsonString = "{\"key\":\"value\"}";
        Resource mockResource = mock(Resource.class);
        when(resourceLoader.getResource("testPath")).thenReturn(mockResource);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(testJsonString.getBytes()));

        JsonNode result = localFileService.readJsonFile("testPath");

        assertNotNull(result);
        assertEquals("value", result.get("key").asText());
    }

    @Test
    public void testListFilesInDirectory() throws IOException {
        // Create a temporary directory and files for the test
        Path tempDir = Files.createTempDirectory("testDir");
        Files.createFile(tempDir.resolve("testFile1.txt"));
        Files.createFile(tempDir.resolve("testFile2.txt"));

        List<String> files = localFileService.listFilesInDirectory(tempDir.toString());

        // Clean up
        Files.deleteIfExists(tempDir.resolve("testFile1.txt"));
        Files.deleteIfExists(tempDir.resolve("testFile2.txt"));
        Files.deleteIfExists(tempDir);

        assertNotNull(files);
        assertTrue(files.contains("testFile1.txt"));
        assertTrue(files.contains("testFile2.txt"));
    }

    @Test
    public void testDeleteFile() throws IOException {
        // Create a temporary file for the test
        Path tempFile = Files.createTempFile("testFile", ".txt");

        boolean result = localFileService.deleteFile(tempFile.toString());

        assertFalse(Files.exists(tempFile)); // Check if file was deleted
        assertTrue(result);
    }

    @Test
    public void testCreateDirectory() throws IOException {
        String tempDirPath = System.getProperty("java.io.tmpdir") + "/testDir";
        boolean result = localFileService.createDirectory(tempDirPath);

        Path tempDir = Paths.get(tempDirPath);
        assertTrue(Files.exists(tempDir) && Files.isDirectory(tempDir));

        // Clean up
        Files.deleteIfExists(tempDir);
        assertTrue(result);
    }

    @Test
    public void testFileExists() throws IOException {
        Path tempFile = Files.createTempFile("testFile", ".txt");

        boolean result = localFileService.fileExists(tempFile.toString());

        assertTrue(result);

        // Clean up
        Files.deleteIfExists(tempFile);
    }

    @Test
    public void testAppendToFile() throws IOException {
        Path tempFile = Files.createTempFile("testFile", ".txt");

        localFileService.appendToFile(tempFile.toString(), "test data");

        String content = Files.readString(tempFile);
        assertTrue(content.contains("test data"));

        // Clean up
        Files.deleteIfExists(tempFile);
    }

    @Test
    public void testWriteJsonToFile() throws IOException {
        Path tempFile = Files.createTempFile("testFile", ".txt");
        JsonNode jsonNode = objectMapper.createObjectNode().put("key", "value");

        localFileService.writeJsonToFile(tempFile.toString(), jsonNode);

        String content = Files.readString(tempFile);
        assertEquals("{\"key\":\"value\"}", content.trim());

        // Clean up
        Files.deleteIfExists(tempFile);
    }

    @Test
    public void testReadHtmlTemplate() {
        String result = localFileService.readHtmlTemplate("test.html");
        assertNotNull(result);
        assertTrue(result.contains("<!DOCTYPE html>")); // Check for some HTML tag
    }
}