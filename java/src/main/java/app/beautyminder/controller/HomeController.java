package app.beautyminder.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class HomeController {

    @GetMapping
    public String root() {
        return "forward:/flutter/index.html";
    }

    @GetMapping("LB")
    public ResponseEntity<String> sayHitLB() {
        return ResponseEntity.ok("good");
    }
}
