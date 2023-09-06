package app.beautyminder.repository;

import app.beautyminder.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // 기본 CRUD 메서드는 JpaRepository에서 이미 제공

    // 사용자 ID로 할 일 목록 조회
    List<Todo> findByUserId(Long userId);

    // 특정 날짜의 할 일 목록 조회
    List<Todo> findByDate(LocalDate date);

    // 특정 사용자의 특정 날짜의 할 일 목록 조회
    List<Todo> findByUserIdAndDate(Long userId, LocalDate date);

    // 특정 기간동안의 할 일 목록 조회
    @Query("SELECT t FROM Todo t WHERE t.date BETWEEN :startDate AND :endDate")
    List<Todo> findBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 특정 사용자의 특정 기간동안의 할 일 목록 조회
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId AND t.date BETWEEN :startDate AND :endDate")
    List<Todo> findBetweenDatesByUserId(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 할 일 내용으로 조회 (LIKE 검색)
    @Query("SELECT t FROM Todo t WHERE t.tasks LIKE %:keyword%")
    List<Todo> findByTaskKeyword(@Param("keyword") String keyword);

    // 특정 사용자의 할 일 내용으로 조회 (LIKE 검색)
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId AND t.tasks LIKE %:keyword%")
    List<Todo> findByTaskKeywordAndUserId(@Param("userId") Long userId, @Param("keyword") String keyword);
}
