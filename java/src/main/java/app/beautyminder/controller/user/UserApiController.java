package app.beautyminder.controller.user;

import app.beautyminder.domain.User;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.dto.user.SignUpResponse;
import app.beautyminder.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user") // Base path for all routes in this controller
public class UserApiController {

    private final UserService userService;

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
    @PostMapping("/admin")
    public ResponseEntity<String> signUpAdmin(@RequestBody AddUserRequest request) {
        try {
            userService.saveAdmin(request);
            return ResponseEntity.ok("Admin created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // User logout
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok("Logged out successfully");
    }

    // Protected endpoint for admin only
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/protected")
    public ResponseEntity<String> adminProtected() {
        return ResponseEntity.ok("Admin protected route accessed");
    }
}
