package app.beautyminder.service.vision;

import com.google.cloud.vision.v1.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
public class VisionService {

    public Optional<String> execute(String url) {
        var totalTime = new StopWatch();
        totalTime.start();

        var requests = new ArrayList<AnnotateImageRequest>();
        var imgSource = ImageSource.newBuilder().setImageUri(url).build();
        var img = Image.newBuilder().setSource(imgSource).build();
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