package app.beautyminder.controller.html;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MyErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            switch(statusCode) {
                case 404:
                    model.addAttribute("error", "Page not found");
                    break;
                case 500:
                    model.addAttribute("error", "Internal server error");
                    break;
                default:
                    model.addAttribute("error", "Unexpected error");
            }
        }

        model.addAttribute("status", status);

        return "error/error"; // This tells Spring Boot to use 'error.html' Thymeleaf template
    }
}