package app.beautyminder.controller;

import app.beautyminder.config.jwt.TokenProvider;
import app.beautyminder.domain.Diary;
import app.beautyminder.domain.User;
import app.beautyminder.dto.DiaryResponse;
import app.beautyminder.dto.LoginResponse;
import app.beautyminder.dto.SignUpResponse;
import app.beautyminder.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class UserViewController {

    private final UserService userService;

    @GetMapping("/")

    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login2")
    public String login2() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/diaries";
        }
        return "login2";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/check")
    public String adminOnlyMethod() {
        return "admin result";
    }

//    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/protected")
    @ResponseBody
    public String someMethod(Authentication auth) {
        System.out.println(auth);
        if (auth == null) {
            return "go login";
        }
        return "accessed protected route";
    }


}
