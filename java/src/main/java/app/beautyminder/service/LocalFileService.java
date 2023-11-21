package app.beautyminder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Service
public class LocalFileService {
    @Qualifier("webApplicationContext")
    private final ResourceLoader resourceLoader;

    private final ObjectMapper objectMapper;

    private static final String TEMP_FILES_PATTERN = "upload_*";
    private static final long TEMP_FILE_LIFETIME = 1000 * 60 * 60; // 1 hour in milliseconds


    public JsonNode readJsonFile(String filePath) throws IOException {
        Resource resource = resourceLoader.getResource(filePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readTree(inputStream);
        } catch (IllegalArgumentException e) {
            return objectMapper.readTree(new File(filePath));
        }
    }

    public List<String> listFilesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        String[] files = directory.list();
        return files != null ? Arrays.asList(files) : new ArrayList<>();
    }

    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }

    public boolean createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        return directory.mkdirs();
    }


    public boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }


    public void appendToFile(String filePath, String data) throws IOException {
        try (FileWriter fw = new FileWriter(filePath, true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {
            out.println(data);
        }
    }

    public void writeJsonToFile(String filePath, JsonNode jsonObject) throws IOException {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(jsonObject.toString());
            file.flush();
        }
    }

    @PreDestroy
    public void onDestroy() {
        cleanupOldTempFiles();
    }

    @Scheduled(fixedRate = TEMP_FILE_LIFETIME)
    public void cleanupOldTempFiles() {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempDir, TEMP_FILES_PATTERN)) {
            for (Path path : directoryStream) {
                try {
                    // Delete the file if it's older than 10 mins
                    if (Files.getLastModifiedTime(path).to(TimeUnit.MILLISECONDS) < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10)) {
                        Files.delete(path);
                    }
                } catch (IOException e) {
                    // Log the exception, possibly rethrow or handle it based on your application's needs
                }
            }
        } catch (IOException e) {
            // Log the exception, possibly rethrow or handle it based on your application's needs
        }
    }
}
