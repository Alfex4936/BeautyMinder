package app.beautyminder.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;

@RequiredArgsConstructor
@Service
public class FileStorageService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String storeFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata));

            URL fileUrl = amazonS3.getUrl(bucket, fileName);
            return fileUrl.toString();
        } catch (IOException e) {
            throw new FileStorageException("Could not store file. Please try again!", e);
        }
    }

    public Resource loadFile(String storedFileName) {
        try {
            S3Object o = amazonS3.getObject(new GetObjectRequest(bucket, storedFileName));
            try (S3ObjectInputStream objectInputStream = o.getObjectContent()) {
                byte[] bytes = IOUtils.toByteArray(objectInputStream);
                return new ByteArrayResource(bytes);
            }
        } catch (AmazonS3Exception | IOException e) {
            throw new FileStorageException("Could not retrieve file. Please try again!", e);
        }
    }
}

class FileStorageException extends RuntimeException {
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}