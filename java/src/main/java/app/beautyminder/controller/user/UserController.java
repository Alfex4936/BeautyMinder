package app.beautyminder.controller.user;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.PasswordResetToken;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.dto.PasswordResetResponse;
import app.beautyminder.dto.sms.SmsResponseDTO;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.dto.user.ForgotPasswordRequest;
import app.beautyminder.dto.user.ResetPasswordRequest;
import app.beautyminder.dto.user.SignUpResponse;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.service.FileStorageService;
import app.beautyminder.service.MongoService;
import app.beautyminder.service.auth.SmsService;
import app.beautyminder.service.auth.TokenService;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticRankService;
import app.beautyminder.util.AuthenticatedUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.function.Predicate.not;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user") // Base path for all routes in this controller
public class UserController {

    private final UserService userService;
    private final MongoService mongoService;
    private final SmsService smsService;
    private final TokenService tokenService;
    private final FileStorageService fileStorageService;

    private final CosmeticRepository cosmeticRepository;
    private final ReviewRepository reviewRepository;
    private final CosmeticRankService cosmeticRankService;

    // Standard user sign-up
    @Operation(summary = "Standard User Signup", description = "표준 사용자 등록을 처리합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User signup details"), tags = {"User Operations"}, responses = {@ApiResponse(responseCode = "200", description = "사용자가 생성됨", content = @Content(schema = @Schema(implementation = SignUpResponse.class))), @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = SignUpResponse.class)))})
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @org.springframework.web.bind.annotation.RequestBody AddUserRequest request) {
        try {
            String userId = userService.saveUser(request).getId();
            User user = userService.findById(userId);
            return ResponseEntity.ok(new SignUpResponse("A user is created", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SignUpResponse(e.getMessage(), null));
        }
    }

    // Admin sign-up
    @Operation(summary = "Admin User Signup", description = "관리자 사용자 등록을 처리합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Admin signup details"), tags = {"User Operations"}, responses = {@ApiResponse(responseCode = "200", description = "관리자가 생성됨", content = @Content(schema = @Schema(implementation = SignUpResponse.class))), @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = SignUpResponse.class)))})
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

    @Operation(summary = "User Logout", description = "사용자 로그아웃", tags = {"User Operations"}, responses = {@ApiResponse(responseCode = "200", description = "성공적으로 로그아웃됨", content = @Content(schema = @Schema(implementation = String.class))), @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)})
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok("Logged out successfully");
    }


    @Operation(summary = "User Deletion", description = "사용자 삭제 by userId [USER 권한 필요]", tags = {"User Operations"})
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteUser(@AuthenticatedUser User user) {
        userService.deleteUserAndRelatedData(user.getId());
        return ResponseEntity.ok("a user is deleted successfully");
    }


    @Operation(summary = "Get user profile", description = "사용자 프로필 가져오기 [USER 권한 필요]", tags = {"User Profile Operations"}, responses = {@ApiResponse(responseCode = "200", description = "유저 데이터 성공적으로 불러옴", content = @Content(schema = @Schema(implementation = User.class))), @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = User.class)))})
    @GetMapping("/me")
    public ResponseEntity<User> getProfile(@AuthenticatedUser User user) {
        try {
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update user profile", description = "사용자 프로필 업데이트 [USER 권한 필요]", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = Map.class)), description = "Profile updates"), tags = {"User Profile Operations"}, responses = {@ApiResponse(responseCode = "200", description = "유저 업데이트 완료", content = @Content(schema = @Schema(implementation = User.class))), @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = User.class)))})

    // org.springframework.web.bind.annotation.
    // Can take any field in User class
    @PatchMapping("/update")
    public ResponseEntity<?> updateProfile(@AuthenticatedUser User user, @RequestBody Map<String, Object> updates) {
        // 전화번호 미리 체크
        if (updates.containsKey("phoneNumber")) {
            String phoneNumber = (String) updates.get("phoneNumber");
            boolean phoneNumberExists = userService.findUserByPhoneNumber(phoneNumber).isPresent();
            if (phoneNumberExists) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Phone number already exists for another user.");
            }
        }

        Optional<User> optionalUser = mongoService.updateFields(user.getId(), updates, User.class);
        if (optionalUser.isPresent()) {
            return ResponseEntity.ok(optionalUser.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

//        return mongoService.updateFields(userId, updates, User.class)
//                .map(user -> new ResponseEntity<User>(user, HttpStatus.OK))
//                .orElseGet(() -> new ResponseEntity<String>("User not found", HttpStatus.BAD_REQUEST));
    }

    @Operation(summary = "Add to User Favorite", description = "사용자의 즐겨찾기에 화장품을 추가합니다. [USER 권한 필요]", tags = {"User Profile Operations"}, parameters = {@Parameter(name = "cosmeticId", description = "화장품의 ID")}, responses = {@ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = User.class))), @ApiResponse(responseCode = "404", description = "사용자 또는 화장품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = String.class)))})
    @PostMapping("/favorites/{cosmeticId}")
    public ResponseEntity<User> addToUserFavorite(@AuthenticatedUser User user, @PathVariable String cosmeticId) {
        try {
            User updatedUser = userService.addCosmeticById(user.getId(), cosmeticId);

            // Redis
            cosmeticRankService.collectFavEvent(cosmeticId);

            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Delete a favourite of User", description = "사용자의 즐겨찾기에 화장품을 삭제합니다. [USER 권한 필요]", tags = {"User Profile Operations"}, parameters = {@Parameter(name = "cosmeticId", description = "화장품의 ID")}, responses = {@ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = User.class))), @ApiResponse(responseCode = "404", description = "사용자 또는 화장품을 찾을 수 없음", content = @Content(schema = @Schema(implementation = String.class)))})
    @DeleteMapping("/favorites/{cosmeticId}")
    public ResponseEntity<User> removeFromUserFavorite(@AuthenticatedUser User user, @PathVariable String cosmeticId) {
        try {
            User updatedUser = userService.removeCosmeticById(user.getId(), cosmeticId);

            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Get favorites of User", description = "사용자의 즐겨찾기를 전부 불러옵니다. [USER 권한 필요]", tags = {"User Profile Operations"}, responses = {@ApiResponse(responseCode = "200", description = "성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Cosmetic.class)))),

    })
    @GetMapping("/favorites")
    public ResponseEntity<List<Cosmetic>> getFavorites(@AuthenticatedUser User user) {
        try {
            // Fetch the actual Cosmetic objects by their IDs
            List<Cosmetic> cosmetics = cosmeticRepository.findAllById(user.getCosmeticIds());

            return ResponseEntity.ok(cosmetics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get reviews of User", description = "사용자의 리뷰를 전부 불러옵니다. [USER 권한 필요]", tags = {"User Profile Operations"}, responses = {@ApiResponse(responseCode = "200", description = "성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Review.class)))),

    })
    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getUserReviews(@AuthenticatedUser User user) {
        try {
            // Fetch all the reviews made by the user
            List<Review> reviews = reviewRepository.findByUser(user);

            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Upload Profile Image", description = "유저 프로필 사진 업로드하기 [USER 권한 필요]", tags = {"User Profile Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Image uploaded successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class), examples = @ExampleObject(name = "Image URL", value = "\"http://example.com/image.jpg\"", summary = "URL of the uploaded image"))), @ApiResponse(responseCode = "400", description = "Invalid user ID or image data"), @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadProfileImage(@AuthenticatedUser User user,

                                     @Parameter(description = "Profile image file to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, examples = @ExampleObject(name = "file", summary = "A 'binary' file"))) @RequestParam("image") MultipartFile image) {
        String imageUrl = fileStorageService.storeFile(image, "profile/");
        mongoService.updateFields(user.getId(), Map.of("profileImage", imageUrl), User.class);

        return imageUrl;
    }

    @Operation(summary = "Get user's search history", description = "유저 검색 기록 얻기 [USER 권한 필요]", tags = {"User Operations"})
    @GetMapping(value = "/search-history")
    public ResponseEntity<?> getKeywordHistory(@AuthenticatedUser User user) {
        return ResponseEntity.status(HttpStatus.OK).body(user.getKeywordHistory());
    }

    /* LOST PASSWORD ----------------------------  */
    @Operation(summary = "Send SMS for password reset", description = "비밀번호 재설정을 위한 SMS 전송 (- 제외한 번호)", tags = {"Password Reset Operations"}, responses = {@ApiResponse(responseCode = "200", description = "SMS 전송 완료", content = @Content(schema = @Schema(implementation = String.class))), @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = String.class)))})
    // FORGOT PASSWORD
    @GetMapping("/sms/send/{phoneNumber}")
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

    @Operation(summary = "Request password reset via email", description = "이메일을 통한 비밀번호 재설정 요청", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = Map.class)), description = "Email for password reset"), tags = {"Password Reset Operations"}, responses = {@ApiResponse(responseCode = "200", description = "비밀번호 재설정 요청 메일 전송 완료", content = @Content(schema = @Schema(implementation = String.class))), @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = String.class)))})
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        try {
            userService.requestPasswordReset(email); // This method should create token and send email
            return ResponseEntity.ok("Password reset email sent");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(summary = "Validate password reset token", description = "비밀번호 재설정 토큰 유효성 검사", tags = {"Password Reset Operations"}, responses = {@ApiResponse(responseCode = "200", description = "토큰 유효함", content = @Content(schema = @Schema(implementation = String.class))), @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = String.class)))})
    @GetMapping("/reset-password")
    public ModelAndView showResetPasswordForm(@RequestParam("token") String token) {
        ModelAndView modelAndView = new ModelAndView("reset-password");
        try {
            PasswordResetToken resetToken = tokenService.validateResetToken(token);
            modelAndView.addObject("email", resetToken.getEmail());
            modelAndView.addObject("token", token);
        } catch (IllegalArgumentException e) {
            // handle invalid token case, maybe setting a specific message or redirecting
            modelAndView.addObject("error", "The reset token is invalid or has expired.");
//            modelAndView.setViewName("error-page");
        }
        return modelAndView;
    }

    @Operation(summary = "Reset Password", description = "사용자의 비밀번호를 재설정합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Reset password details"), tags = {"Password Reset Operations"}, responses = {@ApiResponse(responseCode = "200", description = "비밀번호가 성공적으로 재설정됨", content = @Content(schema = @Schema(implementation = String.class))), @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = String.class)))})
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@ModelAttribute ResetPasswordRequest request) {
        return Optional.ofNullable(request.getToken()).filter(not(String::isBlank)).flatMap(token -> Optional.ofNullable(request.getPassword()).filter(not(String::isBlank)).map(newPassword -> {
            try {
                userService.resetPassword(token, newPassword);
                return ResponseEntity.ok("Password reset successfully");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        })).orElseGet(() -> ResponseEntity.badRequest().body("Token and password are required"));
    }
}
