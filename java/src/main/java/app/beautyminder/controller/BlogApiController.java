package app.beautyminder.controller;

import app.beautyminder.domain.Diary;
import app.beautyminder.dto.AddDiaryRequest;
import app.beautyminder.dto.DiaryResponse;
import app.beautyminder.dto.UpdateDiaryRequest;
import app.beautyminder.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class BlogApiController {

    private final BlogService blogService;

    @PostMapping("/api/diaries")
    public ResponseEntity<Diary> addDiary(@RequestBody AddDiaryRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Diary savedDiary = blogService.save(request, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedDiary);
    }

    @GetMapping("/api/diaries")
    public ResponseEntity<List<DiaryResponse>> findAllDiaries() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        List<DiaryResponse> diaries = blogService.findAll(authentication.getName())
                .stream()
                .map(DiaryResponse::new)
                .toList();

        return ResponseEntity.ok()
                .body(diaries);
    }

    @GetMapping("/api/diaries/{id}")
    public ResponseEntity<DiaryResponse> findDiary(@PathVariable long id) {
        Diary diary = blogService.findById(id);

        return ResponseEntity.ok()
                .body(new DiaryResponse(diary));
    }

    @DeleteMapping("/api/diaries/{id}")
    public ResponseEntity<Void> deleteDiary(@PathVariable long id) {
        blogService.delete(id);

        return ResponseEntity.ok()
                .build();
    }

    @PutMapping("/api/diaries/{id}")
    public ResponseEntity<Diary> updateDiary(@PathVariable long id,
                                               @RequestBody UpdateDiaryRequest request) {
        Diary updatedDiary = blogService.update(id, request);

        return ResponseEntity.ok()
                .body(updatedDiary);
    }

}

