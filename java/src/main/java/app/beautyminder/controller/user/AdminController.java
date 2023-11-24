package app.beautyminder.controller.user;

import app.beautyminder.service.cosmetic.GPTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final GPTService gptService;


    @GetMapping("/hello")
    public String sayHello() {
        return "Hello admin";
    }

    @GetMapping("/gpt")
    public String testLibrary() {
        return gptService.generateNotice("OSNT");
    }

}