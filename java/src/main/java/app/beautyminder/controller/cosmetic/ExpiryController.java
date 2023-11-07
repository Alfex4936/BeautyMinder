package app.beautyminder.controller.cosmetic;

import app.beautyminder.domain.CosmeticExpiry;
import app.beautyminder.repository.CosmeticExpiryRepository;
import app.beautyminder.service.cosmetic.CosmeticExpiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/expiry")
public class ExpiryController {
    private final CosmeticExpiryService cosmeticExpiryService;
    private final CosmeticExpiryRepository cosmeticExpiryRepository;

    @Operation(
            summary = "Create Expiry Item",
            description = "유통기한 아이템 추가합니다. (userId 포함된 데이터)",
            tags = {"Expiry Operations"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "CosmeticExpiry 모델"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = CosmeticExpiry.class))),
            }
    )
    @PostMapping("/create")
    public ResponseEntity<CosmeticExpiry> createCosmeticExpiry(@RequestBody CosmeticExpiry cosmeticExpiry) {
        // TODO: if it has a cosmeticId, loading name/brandName from Cosmetic?
        return ResponseEntity.ok(cosmeticExpiryService.createCosmeticExpiry(cosmeticExpiry));
    }

    @Operation(
            summary = "Get all Expiry Item",
            description = "유저의 모든 유통기한 아이템을 불러옵니다.",
            tags = {"Expiry Operations"},
            parameters = {@Parameter(name = "userId", description = "유저 ID")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = CosmeticExpiry.class, type = "array"))),
            }
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CosmeticExpiry>> getAllCosmeticExpiriesByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(cosmeticExpiryService.getAllCosmeticExpiriesByUserId(userId));
    }

    @Operation(
            summary = "Get all Expiry Item in pages",
            description = "유저의 모든 유통기한 아이템을 Page로 불러옵니다.",
            tags = {"Expiry Operations"},
            parameters = {
                    @Parameter(name = "userId", description = "유저 ID", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "page", description = "Page number to retrieve", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "0")),
                    @Parameter(name = "size", description = "Number of items per page", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "10")),
                    @Parameter(name = "sort", description = "Sorting criteria in the format: property,asc|desc. Default is expiryDate,asc", in = ParameterIn.QUERY, schema = @Schema(defaultValue = "expiryDate,asc"))
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = PageImpl.class,  // use PageImpl as the implementation
                                            type = "object"
                                    )
                            )
                    )
            }
    )
    @GetMapping("/user/page/{userId}")
    public ResponseEntity<Page<CosmeticExpiry>> getPagedAllCosmeticExpiriesByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expiryDate,asc") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort.split(",")[1]), sort.split(",")[0]));
        return ResponseEntity.ok(cosmeticExpiryService.getPagedAllCosmeticExpiriesByUserId(userId, pageable));
    }

    @Operation(
            summary = "Get an expiry item of an user.",
            description = "유저의 특정 expiry 아이템을 가져옵니다.",
            tags = {"Expiry Operations"},
            parameters = {
                    @Parameter(name = "userId", description = "유저 ID", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "expiryId", description = "Expiry Item ID", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = CosmeticExpiry.class
                                    )
                            )
                    )
            }
    )
    @GetMapping("/user/{userId}/expiry/{expiryId}")
    public ResponseEntity<CosmeticExpiry> getCosmeticExpiry(
            @PathVariable String userId, @PathVariable String expiryId) {
        return ResponseEntity.ok(cosmeticExpiryService.getCosmeticExpiry(userId, expiryId));
    }

    @Operation(
            summary = "Put an expiry item of an user.",
            description = "유저의 특정 expiry 아이템을 PUT합니다.",
            tags = {"Expiry Operations"},
            parameters = {
                    @Parameter(name = "userId", description = "유저 ID", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "expiryId", description = "Expiry Item ID", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "CosmeticExpiry 모델"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = CosmeticExpiry.class
                                    )
                            )
                    )
            }
    )
    @PutMapping("/user/{userId}/expiry/{expiryId}")
    public ResponseEntity<CosmeticExpiry> updateCosmeticExpiry(
            @PathVariable String userId, @PathVariable String expiryId, @RequestBody Map<String, Object> updates) {
        return cosmeticExpiryRepository.findByUserIdAndId(userId, expiryId)
                .flatMap(e -> cosmeticExpiryService.updateCosmeticExpiry(e.getId(), updates))
                .map(ResponseEntity::ok) // Map the CosmeticExpiry to a ResponseEntity
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find CosmeticExpiry"));
    }

    @Operation(
            summary = "Delete an expiry item of an user.",
            description = "유저의 특정 expiry 아이템을 DELETE 합니다.",
            tags = {"Expiry Operations"},
            parameters = {
                    @Parameter(name = "userId", description = "유저 ID", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "expiryId", description = "Expiry Item ID", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = CosmeticExpiry.class
                                    )
                            )
                    )
            }
    )
    @DeleteMapping("/user/{userId}/expiry/{expiryId}")
    public ResponseEntity<Void> deleteCosmeticExpiry(@PathVariable String userId, @PathVariable String expiryId) {
        cosmeticExpiryService.deleteCosmeticExpiry(userId, expiryId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Filter expiry items of user.",
            description = "시작/끝 날짜로 필터링된 유저의 expiry 아이템들을 불러옵니다.",
            tags = {"Expiry Operations"},
            parameters = {
                    @Parameter(name = "userId", description = "유저 ID", in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "startDate", description = "시작 날짜", in = ParameterIn.QUERY, schema = @Schema(type = "object", description = "LocalDate YYYY-MM-DD")),
                    @Parameter(name = "endDate", description = "종료 날짜", in = ParameterIn.QUERY, schema = @Schema(type = "object", description = "LocalDate YYYY-MM-DD")),
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = CosmeticExpiry.class,
                                            type = "array"
                                    )
                            )
                    )
            }
    )
    @GetMapping("/user/{userId}/filter")
    public ResponseEntity<List<CosmeticExpiry>> filterCosmeticExpiries(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(cosmeticExpiryService.filterCosmeticExpiries(userId, startDate, endDate));
    }

}
