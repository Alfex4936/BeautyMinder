package app.beautyminder.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FileStorageService {

    // have to consider using a library that can read and verify the image's binary data.
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/jpg",
            "image/gif"
    );
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String storeFile(MultipartFile file, String folderPath) {
        validateImageFileType(file);

        var extension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf('.'));
        var uniqueFileName = UUID.randomUUID() + extension;
        var fileName = folderPath + uniqueFileName;
        try (InputStream fileInputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3.putObject(new PutObjectRequest(bucket, fileName, fileInputStream, metadata));
            URL fileUrl = amazonS3.getUrl(bucket, fileName);


            // Create and store the thumbnail
//            BufferedImage thumbnail = createThumbnail(file);
//            ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
//            ImageIO.write(thumbnail, getExtension(fileName), thumbnailOutputStream);
//            byte[] thumbnailBytes = thumbnailOutputStream.toByteArray();
//            InputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailBytes);
//
//            String thumbnailName = "thumbnail_" + fileName;
//            ObjectMetadata thumbnailMetadata = new ObjectMetadata();
//            thumbnailMetadata.setContentType(file.getContentType());
//            thumbnailMetadata.setContentLength(thumbnailBytes.length);
//            amazonS3.putObject(new PutObjectRequest(bucket, thumbnailName, thumbnailInputStream, thumbnailMetadata));

            return fileUrl.toString();
        } catch (IOException e) {
            throw new FileStorageException("Could not store file. Please try again!", e);
        }
    }

    public Resource loadFile(String storedFileName) {
        try {
            S3Object s3Object  = amazonS3.getObject(new GetObjectRequest(bucket, storedFileName));
            try (S3ObjectInputStream objectInputStream = s3Object .getObjectContent()) {
                byte[] bytes = IOUtils.toByteArray(objectInputStream);
                return new ByteArrayResource(bytes);
            }
        } catch (AmazonS3Exception | IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not retrieve file. Please try again!", e);
        }
    }

    public void deleteFile(String fileName) {
        amazonS3.deleteObject(bucket, fileName);
    }

    private void validateImageFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (!ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new FileStorageException("Invalid file type. Only JPG, PNG, and GIF images are allowed.", null);
        }
    }

    private BufferedImage createThumbnail(MultipartFile file) throws IOException {
        final int THUMBNAIL_WIDTH = 150;

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        double aspectRatio = (double) width / height;

        int newHeight = (int) (THUMBNAIL_WIDTH / aspectRatio);

        BufferedImage thumbnail = new BufferedImage(THUMBNAIL_WIDTH, newHeight, originalImage.getType());

        Graphics2D g = thumbnail.createGraphics();
        g.drawImage(originalImage, 0, 0, THUMBNAIL_WIDTH, newHeight, null);
        g.dispose();

        return thumbnail;
    }

    private String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }

        return extension;
    }
}

class FileStorageException extends RuntimeException {
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}