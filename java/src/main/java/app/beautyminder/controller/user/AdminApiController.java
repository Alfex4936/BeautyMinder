package app.beautyminder.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminApiController {

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello";
    }
}