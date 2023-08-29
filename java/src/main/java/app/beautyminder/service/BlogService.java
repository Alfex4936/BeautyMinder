package app.beautyminder.service;

import app.beautyminder.config.error.exception.ArticleNotFoundException;
import app.beautyminder.domain.Diary;
import app.beautyminder.dto.AddDiaryRequest;
import app.beautyminder.dto.UpdateDiaryRequest;
import app.beautyminder.repository.BlogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BlogService {

    private final BlogRepository blogRepository;

    public Diary save(AddDiaryRequest request, String userName) {
        return blogRepository.save(request.toEntity(userName));
    }

    public List<Diary> findAll(String userName) {
        return blogRepository.findAllByAuthor(userName);
    }


    public Diary findById(long id) {
        return blogRepository.findById(id)
                .orElseThrow(ArticleNotFoundException::new);
    }

    public void delete(long id) {
        Diary diary = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(diary);
        blogRepository.delete(diary);
    }

    @Transactional
    public Diary update(long id, UpdateDiaryRequest request) {
        Diary diary = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(diary);
        diary.update(request.getTitle(), request.getContent());

        return diary;
    }

    // 게시글을 작성한 유저인지 확인
    private static void authorizeArticleAuthor(Diary diary) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!diary.getAuthor().equals(userName)) {
            throw new IllegalArgumentException("not authorized");
        }
    }

}
