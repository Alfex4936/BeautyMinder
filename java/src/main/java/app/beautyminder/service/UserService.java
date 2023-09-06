package app.beautyminder.service;

import app.beautyminder.domain.User;
import app.beautyminder.dto.AddUserRequest;
import app.beautyminder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public Long save(AddUserRequest dto) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // Check if email already exists
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = User.builder()
                .email(dto.getEmail())
                .password(encoder.encode(dto.getPassword()))
//                .nickname(dto.getNickname())
                .build();

        user.addAuthority("ROLE_USER"); // "ROLE_USER" 권한을 기본으로 설정
        return userRepository.save(user).getId();
    }

    public Long saveAdmin(AddUserRequest dto) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        User admin = User.builder()
                .email(dto.getEmail())
                .password(encoder.encode(dto.getPassword()))
                .build();
        admin.addAuthority("ROLE_ADMIN");  // 관리자 권한 추가
        return userRepository.save(admin).getId();
    }


    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }


}
