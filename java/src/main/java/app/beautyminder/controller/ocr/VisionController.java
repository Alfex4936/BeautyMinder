package app.beautyminder.controller.ocr;

import app.beautyminder.dto.expiry.VisionRequestDTO;
import app.beautyminder.dto.expiry.VisionResponseDTO;
import app.beautyminder.service.vision.VisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/vision")
@RequiredArgsConstructor
public class VisionController {
    private final VisionService visionService;

    @PostMapping("/ocr")
    public ResponseEntity<VisionResponseDTO> parseImageByGoogleVision(@RequestBody VisionRequestDTO request) {
        return visionService.execute(request.getUrl())
                .map(date -> ResponseEntity.ok(new VisionResponseDTO(date, "Success")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new VisionResponseDTO("", "No expiration date found or error occurred")));
    }
}
