package app.beautyminder.controller.ocr;

import app.beautyminder.dto.expiry.VisionResponseDTO;
import app.beautyminder.service.vision.VisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("/vision")
@RequiredArgsConstructor
public class VisionController {
    private final VisionService visionService;

    @PostMapping("/ocr")
    public ResponseEntity<VisionResponseDTO> parseImageByGoogleVision(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "url", required = false) String imageUrl) {

        Optional<String> date;

        if (image != null && !image.isEmpty()) {
            // Convert MultipartFile to Base64 String
            String base64Image;
            try {
                base64Image = Base64.getEncoder().encodeToString(image.getBytes());
            } catch (IOException e) {
                // Handle the exception, perhaps log it and return an appropriate response
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new VisionResponseDTO("?", "Error processing the image file"));
            }
            // Handle MultipartFile by passing Base64 string to service
            date = visionService.execute(base64Image);
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            // Handle URL
            date = visionService.execute(imageUrl);
        } else {
            return ResponseEntity.badRequest()
                    .body(new VisionResponseDTO("?", "No image or URL provided"));
        }

        return date
                .map(d -> ResponseEntity.ok(new VisionResponseDTO(d, "Success")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new VisionResponseDTO("?", "No expiration date found or error occurred")));
    }

}
