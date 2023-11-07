package app.beautyminder.controller.cosmetic;

import app.beautyminder.domain.CosmeticExpiry;
import app.beautyminder.repository.CosmeticExpiryRepository;
import app.beautyminder.service.RecommendService;
import app.beautyminder.service.cosmetic.CosmeticExpiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recommend")
public class RecommendController {
    private final RecommendService recommendService;


    @GetMapping("/{userId}")
    public ResponseEntity<?> getRecommendation(@PathVariable String userId) {
        return ResponseEntity.ok(recommendService.recommendProducts(userId));
    }
}
