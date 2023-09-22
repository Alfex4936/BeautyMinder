package app.beautyminder.service;

import app.beautyminder.domain.User;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();  // 비용이 높은 작업


    // 일반 사용자 저장
    public String saveUser(AddUserRequest dto) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이메일이 이미 사용 중입니다.");
        }
        // 사용자 생성
        User user = User.builder()
                .email(dto.getEmail())
                .password(encoder.encode(dto.getPassword()))
                .build();

        // 기본 권한 설정 ("ROLE_USER")
        user.addAuthority("ROLE_USER");
        return userRepository.save(user).getId();
    }

    // 관리자 저장
    public String saveAdmin(AddUserRequest dto) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이메일이 이미 사용 중입니다.");
        }
        // 관리자 생성
        User admin = User.builder()
                .email(dto.getEmail())
                .password(encoder.encode(dto.getPassword()))
                .build();

        // 관리자 권한 추가
        admin.addAuthority("ROLE_ADMIN");
        return userRepository.save(admin).getId();
    }

    // 사용자 ID로 조회
    public User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }

    // 이메일로 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }

    // 닉네임으로 조회
    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }

    // 프로필 이미지가 있는 사용자 조회
    public List<User> findUsersWithProfileImage() {
        return userRepository.findByProfileImageIsNotNull();
    }

    // 특정 날짜 이후에 생성된 사용자 조회
    public List<User> findUsersCreatedAfter(LocalDateTime date) {
        return userRepository.findByCreatedAtAfter(date);
    }

    // 이메일이나 닉네임으로 조회
    public User findByEmailOrNickname(String email, String nickname) {
        return userRepository.findByEmailOrNickname(email, nickname)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }

    // 권한으로 사용자 목록 조회
    public List<User> findUsersByAuthority(String authority) {
        return userRepository.findByAuthority(authority);
    }

    // 이메일과 비밀번호로 사용자 조회 (로그인)
    public User findByEmailAndPassword(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new IllegalArgumentException("이메일 혹은 비밀번호가 틀립니다."));
    }
}
