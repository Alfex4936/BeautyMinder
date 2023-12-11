package app.beautyminder.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping
    public String root() {
        return "forward:/flutter/index.html";
    }

    @GetMapping("dashboard")
    public String adminRoot() {
        return "forward:/dashboard/index.html";
    }

    @GetMapping("LB")
    public ResponseEntity<String> sayHitLB() {
        return ResponseEntity.ok("good");
    }
}
