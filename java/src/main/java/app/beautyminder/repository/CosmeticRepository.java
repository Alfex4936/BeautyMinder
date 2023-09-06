package app.beautyminder.repository;

import app.beautyminder.domain.Cosmetic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CosmeticRepository extends JpaRepository<Cosmetic, Long> {

    // 기본 CRUD 메서드는 JpaRepository에서 이미 제공

    // 사용자 ID로 화장품 목록 조회
    List<Cosmetic> findByUserId(Long userId);

    // 카테고리로 화장품 목록 조회
    List<Cosmetic> findByCategory(Cosmetic.Category category);

    // 상태로 화장품 목록 조회
    List<Cosmetic> findByStatus(Cosmetic.Status status);

    // 만료일이 임박한 화장품 목록 조회
    @Query("SELECT c FROM Cosmetic c WHERE c.expirationDate <= :date")
    List<Cosmetic> findExpiringSoon(@Param("date") LocalDate date);

    // 특정 사용자의 만료일이 임박한 화장품 목록 조회
    @Query("SELECT c FROM Cosmetic c WHERE c.user.id = :userId AND c.expirationDate <= :date")
    List<Cosmetic> findExpiringSoonByUserId(@Param("userId") Long userId, @Param("date") LocalDate date);

    // 특정 이름의 화장품 조회
    Optional<Cosmetic> findByNameAndUserId(String name, Long userId);

    // 화장품 구매일로 조회
    List<Cosmetic> findByPurchasedDate(LocalDate purchasedDate);

    // 화장품 만료일로 조회
    List<Cosmetic> findByExpirationDate(LocalDate expirationDate);
}
