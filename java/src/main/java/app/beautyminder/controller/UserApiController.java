package app.beautyminder.controller;

import app.beautyminder.domain.Diary;
import app.beautyminder.domain.User;
import app.beautyminder.dto.AddUserRequest;
import app.beautyminder.dto.DiaryResponse;
import app.beautyminder.dto.SignUpResponse;
import app.beautyminder.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
public class UserApiController {

    private final UserService userService;


    @PostMapping("/user")
    public ResponseEntity<SignUpResponse> signup(@RequestBody AddUserRequest request, RedirectAttributes redirectAttributes) {
        System.out.println(request.getEmail());
        System.out.println(request.getPassword());


        try {
            // 권한 추가 예시
//            user.getAuthorities().add("ROLE_ADMIN");
            // 권한 삭제 예시
//            user.getAuthorities().remove("ROLE_USER");

            Long userId = userService.saveUser(request);
            User user = userService.findById(userId);

//            return "redirect:/diaries";
            return ResponseEntity.ok()
                    .body(new SignUpResponse("A user is created", user));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            return "redirect:/login";
            return ResponseEntity.badRequest()
                    .body(null);
        }
    }

    @PostMapping("/admin")
    public String signupAdmin(AddUserRequest request, RedirectAttributes redirectAttributes) {
        try {
            userService.saveAdmin(request);
            return "redirect:/admin/dashboard";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/login";
    }

}
