package app.beautyminder.controller.user;

import app.beautyminder.domain.User;
import app.beautyminder.dto.sms.MessageDTO;
import app.beautyminder.dto.sms.SmsResponseDTO;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.dto.user.ResetPasswordRequest;
import app.beautyminder.dto.user.SignUpResponse;
import app.beautyminder.service.auth.SmsService;
import app.beautyminder.service.auth.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user") // Base path for all routes in this controller
public class UserController {

    private final UserService userService;
    private final SmsService smsService;

    // Standard user sign-up
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody AddUserRequest request) {
        try {
            String userId = userService.saveUser(request);
            User user = userService.findById(userId);
            return ResponseEntity.ok(new SignUpResponse("A user is created", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SignUpResponse(e.getMessage(), null));
        }
    }

    // Admin sign-up
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

    // User logout
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok("Logged out successfully");
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        userService.deleteUserAndRelatedData(userId);
        return ResponseEntity.ok("a user is deleted successfully");
    }


    @GetMapping("/me/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable String userId) {
        try {
            User user = userService.findById(userId);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/update/{userId}")
    public ResponseEntity<User> updateProfile(@PathVariable String userId, @RequestBody Map<String, Object> updates) {
        try {
            User user = userService.findById(userId);
            user = userService.updateUser(user, updates);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // FORGOT PASSSWORD
    @PostMapping("/sms/send")
    public ResponseEntity<String> sendSms(@RequestBody MessageDTO messageDto) throws JsonProcessingException, RestClientException, URISyntaxException, InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException {
        SmsResponseDTO response = smsService.sendSms(messageDto);
        return ResponseEntity.ok(response.toString());
    }

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


    @GetMapping("/reset-password")
    public ResponseEntity<String> validateResetToken(@RequestParam("token") String token) {
        try {
            userService.validateResetToken(token);
            return ResponseEntity.ok("Token is valid");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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
