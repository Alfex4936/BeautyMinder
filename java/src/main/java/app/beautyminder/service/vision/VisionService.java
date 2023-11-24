package app.beautyminder.service.vision;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class VisionService {

    public Optional<String> execute(String input) {
        var img = input.startsWith("http") ? buildImageFromUrl(input) : buildImageFromFile(input);
        return processImage(img);
    }

    private Image buildImageFromUrl(String url) {
        var imgSource = ImageSource.newBuilder().setImageUri(url).build();
        return Image.newBuilder().setSource(imgSource).build();
    }

    private Image buildImageFromFile(String base64EncodedFile) {
        ByteString imgBytes = ByteString.copyFrom(Base64.getDecoder().decode(base64EncodedFile));
        return Image.newBuilder().setContent(imgBytes).build();
    }

    private Optional<String> processImage(Image img) {
        var feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        var request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        var requests = List.of(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            var response = client.batchAnnotateImages(requests);
            for (var res : response.getResponsesList()) {
                if (res.hasError()) {
                    log.error("Error: {}", res.getError().getMessage());
                    return Optional.empty();
                }

                for (var annotation : res.getTextAnnotationsList()) {
                    var ocrResult = annotation.getDescription();
//                    for (var line : ocrResult.split("\\s")) {

                        Optional<String> expirationDate = ExpirationDateExtractor.extractExpirationDate(ocrResult);
                        if (expirationDate.isPresent()) {
                            return expirationDate; // Returns the first found date from the method
//                        }
                    }
                }
            }
        } catch (Exception exception) {
            log.error("Exception occurred: {}", exception.getMessage());
            return Optional.empty();
        }
        return Optional.empty();
    }

    public Optional<String> executeWithMultipartFile(MultipartFile file) {
        var totalTime = new StopWatch();
        totalTime.start();

        var requests = new ArrayList<AnnotateImageRequest>();

        ByteString imgBytes;
        try {
            imgBytes = ByteString.copyFrom(file.getBytes());
        } catch (IOException e) {
            log.error("Error reading image file: {}", e.getMessage());
            return Optional.empty();
        }

        var img = Image.newBuilder().setContent(imgBytes).build();
        var feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        var request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            var response = client.batchAnnotateImages(requests);
            var result = new StringBuilder();

            for (var res : response.getResponsesList()) {
                if (res.hasError()) {
                    log.error("Error: {}", res.getError().getMessage());
                    return Optional.empty();
                }

                res.getTextAnnotationsList().forEach(annotation ->
                        result.append(annotation.getDescription()).append(" ")
                );
            }

            totalTime.stop();
            log.info("BEMINDER: OCR Total Time : {}ms", totalTime.getTotalTimeMillis());

            String ocrResult = result.toString();
            log.info("BEMINDER: OCR RESULT {}", ocrResult);
            Optional<String> expirationDate = ExpirationDateExtractor.extractExpirationDate(ocrResult);
            if (expirationDate.isPresent()) {
                log.info("BEMINDER: Expiration Date: {}", expirationDate.get());
                return expirationDate;
            } else {
                log.info("No expiration date found in the text.");
                return Optional.empty();
            }
        } catch (Exception exception) {
            log.error("Exception occurred: {}", exception.getMessage());
            return Optional.empty();
        }
    }
}