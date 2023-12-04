package app.beautyminder.controller.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import app.beautyminder.service.cosmetic.CosmeticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cosmetic")
public class CosmeticController {

    private final CosmeticService cosmeticService;
    private final CosmeticRankService cosmeticRankService;

    @Operation(summary = "Get All Cosmetics", description = "모든 화장품을 가져옵니다.", tags = {"Cosmetic Operations"}, responses = {@ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = Cosmetic.class, type = "array"))), @ApiResponse(responseCode = "404", description = "화장품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = String.class)))})
    @GetMapping
    public ResponseEntity<List<Cosmetic>> getAllCosmetics() {
        List<Cosmetic> cosmetics = cosmeticService.getAllCosmetics();
        return ResponseEntity.ok(cosmetics);
    }

    @Operation(summary = "Get All Cosmetics in Page", description = "모든 화장품을 가져옵니다. 페이지 형식", tags = {"Cosmetic Operations"}, responses = {@ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = Cosmetic.class, type = "array"))), @ApiResponse(responseCode = "404", description = "화장품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = String.class)))})
    @GetMapping("/page")
    public ResponseEntity<Page<Cosmetic>> getAllCosmeticsByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(cosmeticService.getAllCosmeticsInPage(pageable));
    }

    @Operation(summary = "Get Cosmetic by ID", description = "ID로 화장품을 가져옵니다.", tags = {"Cosmetic Operations"}, parameters = {@Parameter(name = "id", description = "화장품의 ID")}, responses = {@ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = Cosmetic.class))), @ApiResponse(responseCode = "404", description = "화장품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = String.class)))})
    @GetMapping("/{id}")
    public ResponseEntity<Cosmetic> getCosmeticById(@PathVariable String id) {
        Cosmetic cosmetic = cosmeticService.getCosmeticById(id);
        if (cosmetic == null) {
            return ResponseEntity.notFound().build();
        }
        cosmeticRankService.collectClickEvent(id);
        return ResponseEntity.ok(cosmetic);
    }

    // Add a new cosmetic
    @Operation(summary = "Create a cosmetic data", description = "화장품을 생성합니다.", tags = {"Cosmetic Operations"}, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "화장품"), responses = {@ApiResponse(responseCode = "200", description = "화장품 추가 성공", content = @Content(schema = @Schema(implementation = Cosmetic.class))),})
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Cosmetic> createCosmetic(@Valid @RequestBody Cosmetic cosmetic) {
        Cosmetic newCosmetic = cosmeticService.saveCosmetic(cosmetic);
        return ResponseEntity.ok(newCosmetic);
    }

    // Update an existing cosmetic
    @Operation(summary = "Update cosmetic entirely", description = "Cosmetic을 업데이트 합니다. (통째로) [ADMIN 권한 필요]", tags = {"Cosmetic Operations"}, parameters = {@Parameter(name = "id", description = "화장품의 ID"),}, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Cosmetic 모델"), responses = {@ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = Cosmetic.class))), @ApiResponse(responseCode = "404", description = "화장품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = String.class)))})
    @PutMapping("/{id}")
    public ResponseEntity<Cosmetic> updateCosmetic(@PathVariable String id, @RequestBody Cosmetic cosmeticDetails) {
        Cosmetic updatedCosmetic = cosmeticService.updateCosmetic(id, cosmeticDetails);
        if (updatedCosmetic == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedCosmetic);
    }

    // Delete a cosmetic
    @Operation(summary = "Delete a cosmetic entirely", description = "Cosmetic을 삭제 합니다. [ADMIN 권한 필요]", tags = {"Cosmetic Operations"}, parameters = {@Parameter(name = "id", description = "화장품의 ID"),}, responses = {@ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema())), @ApiResponse(responseCode = "404", description = "화장품을 찾을 수 없음", content = @Content(schema = @Schema()))})
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCosmetic(@PathVariable String id) {
        boolean deleted = cosmeticService.deleteCosmetic(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cosmetic Redis click++", description = "Cosmetic click++", tags = {"Redis Operations"}, parameters = {@Parameter(name = "cosmeticId", description = "화장품의 ID"),}, responses = {@ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema()))})
    @PostMapping("/click/{cosmeticId}")
    public ResponseEntity<Void> incrementClickCount(@PathVariable String cosmeticId) {
        cosmeticRankService.collectClickEvent(cosmeticId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cosmetic Redis search hit++", description = "Cosmetic searchHit++", tags = {"Redis Operations"}, parameters = {@Parameter(name = "cosmeticId", description = "화장품의 ID"),}, responses = {@ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema()))})
    @PostMapping("/hit/{cosmeticId}")
    public ResponseEntity<Void> incrementHitCount(@PathVariable String cosmeticId) {
        cosmeticRankService.collectHitEvent(cosmeticId);
        return ResponseEntity.ok().build();
    }

}
