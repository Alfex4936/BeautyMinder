package app.beautyminder.controller;

import app.beautyminder.domain.Diary;
import app.beautyminder.dto.DiaryListViewResponse;
import app.beautyminder.dto.DiaryViewResponse;
import app.beautyminder.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class BlogViewController {

    private final BlogService blogService;

    @GetMapping("/diaries")
    public String getDiaries(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        List<DiaryListViewResponse> diaries = Collections.emptyList();

        if (authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            System.out.println("====== AUTH user: " + username);
            diaries = blogService.findAll(username)
                    .stream()
                    .map(DiaryListViewResponse::new)
                    .toList();
        } else {
            return "redirect:/login"; // Redirect to login
        }
        model.addAttribute("diaries", diaries);

        return "diaryList";
    }


    @GetMapping("/diaries/{id}")
    public String getDiary(@PathVariable Long id, Model model) {
        Diary diary = blogService.findById(id);
        model.addAttribute("diary", new DiaryViewResponse(diary));

        return "diary";
    }


    @GetMapping("/new-diary")
    public String newDiary(@RequestParam(required = false) Long id, Model model) {
        if (id == null) {
            model.addAttribute("diary", new DiaryViewResponse());
        } else {
            Diary diary = blogService.findById(id);
            model.addAttribute("diary", new DiaryViewResponse(diary));
        }

        return "newDiary";
    }
}
