package app.beautyminder.repository;

import app.beautyminder.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 기본 CRUD 메서드는 JpaRepository에서 이미 제공

    // 특정 사용자 ID로 리프레시 토큰 찾기
    Optional<RefreshToken> findByUserId(Long userId);

    // 리프레시 토큰 값으로 찾기
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    // 만료된 리프레시 토큰 모두 찾기
    @Query("SELECT r FROM RefreshToken r WHERE r.expiresAt < :now")
    List<RefreshToken> findAllExpiredTokens(@Param("now") LocalDateTime now);

    // 만료된 리프레시 토큰 삭제
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now")
    void deleteAllExpiredTokens(@Param("now") LocalDateTime now);

    // 특정 사용자의 리프레시 토큰 갱신
    @Modifying
    @Query("UPDATE RefreshToken r SET r.refreshToken = :newToken WHERE r.user.id = :userId")
    void updateRefreshTokenByUserId(@Param("userId") Long userId, @Param("newToken") String newToken);

    // 특정 사용자의 리프레시 토큰 삭제
    void deleteByUserId(Long userId);

    // 리프레시 토큰 강제 만료 처리
    default void revokeRefreshToken(Long userId) {
        deleteByUserId(userId);
    }
}
