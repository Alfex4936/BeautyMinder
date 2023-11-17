package app.beautyminder.controller.cosmetic;

import app.beautyminder.domain.CosmeticExpiry;
import app.beautyminder.domain.User;
import app.beautyminder.dto.expiry.AddExpiryProduct;
import app.beautyminder.repository.CosmeticExpiryRepository;
import app.beautyminder.service.cosmetic.CosmeticExpiryService;
import app.beautyminder.util.AuthenticatedUser;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/expiry")
@PreAuthorize("hasRole('ROLE_USER')")
public class ExpiryController {
    private final CosmeticExpiryService cosmeticExpiryService;

    @Operation(
            summary = "Create Expiry Item",
            description = "유통기한 아이템 추가합니다. [USER 권한 필요]",
            tags = {"Expiry Operations"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "CosmeticExpiry 모델"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = CosmeticExpiry.class))),
            }
    )
    @PostMapping("/create")
    public ResponseEntity<CosmeticExpiry> createCosmeticExpiry(@RequestBody AddExpiryProduct cosmeticExpiry, @Parameter(hidden = true) @AuthenticatedUser User user) {
        // TODO: if it has a cosmeticId, loading name/brandName from Cosmetic?
        return ResponseEntity.ok(cosmeticExpiryService.createCosmeticExpiry(user.getId(), cosmeticExpiry));
    }

    @Operation(
            summary = "Get all Expiry Item",
            description = "유저의 모든 유통기한 아이템을 불러옵니다. [USER 권한 필요]",
            tags = {"Expiry Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = CosmeticExpiry.class, type = "array"))),
            }
    )
    @GetMapping()
    public ResponseEntity<List<CosmeticExpiry>> getAllCosmeticExpiriesByUserId(@Parameter(hidden = true) @AuthenticatedUser User user) {
        return ResponseEntity.ok(cosmeticExpiryService.getAllCosmeticExpiriesByUserId(user.getId()));
    }

    @Operation(
            summary = "Get all Expiry Item in pages",
            description = "유저의 모든 유통기한 아이템을 Page로 불러옵니다. [USER 권한 필요]",
            tags = {"Expiry Operations"},
            parameters = {
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
    @GetMapping("/page")
    public ResponseEntity<Page<CosmeticExpiry>> getPagedAllCosmeticExpiriesByUserId(
            @Parameter(hidden = true) @AuthenticatedUser User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expiryDate,asc") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort.split(",")[1]), sort.split(",")[0]));
        return ResponseEntity.ok(cosmeticExpiryService.getPagedAllCosmeticExpiriesByUserId(user.getId(), pageable));
    }

    @Operation(
            summary = "Get an expiry item of an user.",
            description = "유저의 특정 expiry 아이템을 가져옵니다. [USER 권한 필요]",
            tags = {"Expiry Operations"},
            parameters = {
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
    @GetMapping("/expiry/{expiryId}")
    public ResponseEntity<CosmeticExpiry> getCosmeticExpiry(
            @Parameter(hidden = true) @AuthenticatedUser User user, @PathVariable String expiryId) {
        return ResponseEntity.ok(cosmeticExpiryService.getCosmeticExpiry(user.getId(), expiryId));
    }

    @Operation(
            summary = "Put an expiry item of an user.",
            description = "유저의 특정 expiry 아이템을 PUT합니다. [USER 권한 필요]",
            tags = {"Expiry Operations"},
            parameters = {
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
    @PutMapping("/expiry/{expiryId}")
    public ResponseEntity<CosmeticExpiry> updateCosmeticExpiry(
            @Parameter(hidden = true) @AuthenticatedUser User user, @PathVariable String expiryId, @RequestBody Map<String, Object> updates) {
        return cosmeticExpiryService.findByUserIdAndId(user.getId(), expiryId)
                .flatMap(e -> cosmeticExpiryService.updateCosmeticExpiry(e.getId(), updates))
                .map(ResponseEntity::ok) // Map the CosmeticExpiry to a ResponseEntity
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find CosmeticExpiry"));
    }

    @Operation(
            summary = "Delete an expiry item of an user.",
            description = "유저의 특정 expiry 아이템을 DELETE 합니다. [USER 권한 필요]",
            tags = {"Expiry Operations"},
            parameters = {
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
    @DeleteMapping("/expiry/{expiryId}")
    public ResponseEntity<Void> deleteCosmeticExpiry(@Parameter(hidden = true) @AuthenticatedUser User user, @PathVariable String expiryId) {
        cosmeticExpiryService.deleteCosmeticExpiry(user.getId(), expiryId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Filter expiry items of user.",
            description = "시작/끝 날짜로 필터링된 유저의 expiry 아이템들을 불러옵니다. [USER 권한 필요]",
            tags = {"Expiry Operations"},
            parameters = {
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
    @GetMapping("/filter")
    public ResponseEntity<List<CosmeticExpiry>> filterCosmeticExpiries(
            @Parameter(hidden = true) @AuthenticatedUser User user,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(cosmeticExpiryService.filterCosmeticExpiries(user.getId(), startDate, endDate));
    }

}
