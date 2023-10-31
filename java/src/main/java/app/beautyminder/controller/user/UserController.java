package app.beautyminder.controller.user;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.dto.PasswordResetResponse;
import app.beautyminder.dto.sms.SmsResponseDTO;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.dto.user.ResetPasswordRequest;
import app.beautyminder.dto.user.SignUpResponse;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.service.auth.SmsService;
import app.beautyminder.service.auth.TokenService;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import app.beautyminder.service.cosmetic.CosmeticService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user") // Base path for all routes in this controller
public class UserController {

    private final UserService userService;
    private final SmsService smsService;
    private final TokenService tokenService;
    private final CosmeticRepository cosmeticRepository;
    private final ReviewRepository reviewRepository;
    private final CosmeticRankService cosmeticRankService;

    // Standard user sign-up
    @Operation(
            summary = "Standard User Signup",
            description = "표준 사용자 등록을 처리합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User signup details"),
            tags = {"User Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용자가 생성됨", content = @Content(schema = @Schema(implementation = SignUpResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = SignUpResponse.class)))
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @org.springframework.web.bind.annotation.RequestBody AddUserRequest request) {
        try {
            String userId = userService.saveUser(request);
            User user = userService.findById(userId);
            return ResponseEntity.ok(new SignUpResponse("A user is created", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SignUpResponse(e.getMessage(), null));
        }
    }

    // Admin sign-up
    @Operation(
            summary = "Admin User Signup",
            description = "관리자 사용자 등록을 처리합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Admin signup details"),
            tags = {"User Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "관리자가 생성됨", content = @Content(schema = @Schema(implementation = SignUpResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = SignUpResponse.class)))
            }
    )
    @PostMapping("/signup-admin")
    public ResponseEntity<SignUpResponse> signUpAdmin(@RequestBody AddUserRequest request) {
        try {
            String userId = userService.saveAdmin(request);
            User user = userService.findById(userId);
            return ResponseEntity.ok(new SignUpResponse("A user is created", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SignUpResponse(e.getMessage(), null));
        }
    }

    @Operation(
            summary = "User Logout",
            description = "사용자 로그아웃",
            tags = {"User Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 로그아웃됨", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
            }
    )
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok("Logged out successfully");
    }


    @Operation(
            summary = "User Deletion",
            description = "사용자 삭제 by userId",
            tags = {"User Operations"}
    )
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        userService.deleteUserAndRelatedData(userId);
        return ResponseEntity.ok("a user is deleted successfully");
    }


    @Operation(
            summary = "Get user profile",
            description = "사용자 프로필 가져오기",
            tags = {"User Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "유저 데이터 성공적으로 불러옴", content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = User.class)))
            }
    )
    @GetMapping("/me/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable String userId) {
        try {
            User user = userService.findById(userId);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Update user profile",
            description = "사용자 프로필 업데이트",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = Map.class)), description = "Profile updates"),
            tags = {"User Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "유저 업데이트 완료", content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = User.class)))
            }
    )

    // org.springframework.web.bind.annotation.
    // Can take any field in User class
    @PatchMapping("/update/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable String userId, @RequestBody Map<String, Object> updates) {
        Optional<User> optionalUser = userService.updateUserFields(userId, updates);
        if (optionalUser.isPresent()) {
            return ResponseEntity.ok(optionalUser.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @Operation(
            summary = "Add to User Favorite",
            description = "사용자의 즐겨찾기에 화장품을 추가합니다.",
            tags = {"User Operations"},
            parameters = {
                    @Parameter(name = "userId", description = "사용자의 ID"),
                    @Parameter(name = "cosmeticId", description = "화장품의 ID")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "사용자 또는 화장품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping("/{userId}/favorites/{cosmeticId}")
    public ResponseEntity<User> addToUserFavorite(@PathVariable String userId, @PathVariable String cosmeticId) {
        try {
            User updatedUser = userService.addCosmeticById(userId, cosmeticId);

            // Redis
            cosmeticRankService.collectFavEvent(cosmeticId);

            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
            summary = "Delete a favourite of User",
            description = "사용자의 즐겨찾기에 화장품을 삭제합니다.",
            tags = {"User Operations"},
            parameters = {
                    @Parameter(name = "userId", description = "사용자의 ID"),
                    @Parameter(name = "cosmeticId", description = "화장품의 ID")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "사용자 또는 화장품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @DeleteMapping("/{userId}/favorites/{cosmeticId}")
    public ResponseEntity<User> removeFromUserFavorite(@PathVariable String userId, @PathVariable String cosmeticId) {
        try {
            User updatedUser = userService.removeCosmeticById(userId, cosmeticId);

            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{userId}/favorites")
    public ResponseEntity<List<Cosmetic>> getFavorites(@PathVariable String userId) {
        try {
            User user = userService.findById(userId);

            // Fetch the actual Cosmetic objects by their IDs
            List<Cosmetic> cosmetics = cosmeticRepository.findAllById(user.getCosmeticIds());

            return ResponseEntity.ok(cosmetics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{userId}/reviews")
    public ResponseEntity<List<Review>> getUserReviews(@PathVariable String userId) {
        try {
            User user = userService.findById(userId);

            // Fetch all the reviews made by the user
            List<Review> reviews = reviewRepository.findByUser(user);

            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Send SMS for password reset",
            description = "비밀번호 재설정을 위한 SMS 전송",
            tags = {"Password Reset Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "SMS 전송 완료", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    // FORGOT PASSWORD
    @PostMapping("/sms/send/{phoneNumber}")
    public ResponseEntity<String> sendSms(@PathVariable String phoneNumber) throws JsonProcessingException, RestClientException, URISyntaxException, InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException {
        // TODO: check user's phone number and change content
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Phone number is required");
        }

        try {
            PasswordResetResponse tUser = userService.requestPasswordResetByNumber(phoneNumber);
            SmsResponseDTO response = smsService.sendSms(tUser);
            return ResponseEntity.ok(response.toString());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Request password reset via email",
            description = "이메일을 통한 비밀번호 재설정 요청",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = Map.class)), description = "Email for password reset"),
            tags = {"Password Reset Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "비밀번호 재설정 요청 메일 전송 완료", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        try {
            userService.requestPasswordReset(email);  // This method should create token and send email
            return ResponseEntity.ok("Password reset email sent");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(
            summary = "Validate password reset token",
            description = "비밀번호 재설정 토큰 유효성 검사",
            tags = {"Password Reset Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 유효함", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @GetMapping("/reset-password")
    public ResponseEntity<String> validateResetToken(@RequestParam("token") String token) {
        try {
            tokenService.validateResetToken(token);
            return ResponseEntity.ok("Token is valid");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Reset Password",
            description = "사용자의 비밀번호를 재설정합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Reset password details"),
            tags = {"Password Reset Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "비밀번호가 성공적으로 재설정됨", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        String token = request.getToken();
        String newPassword = request.getPassword();
        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Token and password are required");
        }

        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Password reset successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
