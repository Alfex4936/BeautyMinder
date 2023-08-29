package app.beautyminder.repository;

import app.beautyminder.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogRepository extends JpaRepository<Diary, Long> {
    List<Diary> findAllByAuthor(String author);
}

