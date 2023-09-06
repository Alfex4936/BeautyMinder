package app.beautyminder.repository;

import app.beautyminder.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    // 기본 CRUD 메서드는 JpaRepository에서 이미 제공

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 닉네임으로 사용자 조회
    Optional<User> findByNickname(String nickname);

    // 프로필 이미지가 있는 사용자 조회
    List<User> findByProfileImageIsNotNull();

    // 특정 날짜 이후에 생성된 사용자 조회
    List<User> findByCreatedAtAfter(LocalDateTime date);

    // 이메일이나 닉네임으로 사용자 조회
    @Query("SELECT u FROM User u WHERE u.email = :email OR u.nickname = :nickname")
    Optional<User> findByEmailOrNickname(@Param("email") String email, @Param("nickname") String nickname);

    // 이메일과 비밀번호로 사용자 조회 (로그인)
    Optional<User> findByEmailAndPassword(String email, String password);

    // 권한으로 사용자 목록 조회
    @Query("SELECT u FROM User u WHERE :authority MEMBER OF u.authorities")
    List<User> findByAuthority(@Param("authority") String authority);
}