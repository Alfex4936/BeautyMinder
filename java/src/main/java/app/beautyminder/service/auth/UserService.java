package app.beautyminder.service.auth;

import app.beautyminder.domain.PasswordResetToken;
import app.beautyminder.domain.User;
import app.beautyminder.dto.PasswordResetResponse;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.repository.PasswordResetTokenRepository;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.repository.TodoRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.FileStorageService;
import app.beautyminder.service.review.ReviewService;
import app.beautyminder.service.cosmetic.CosmeticExpiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final ReviewService reviewService;
    private final CosmeticExpiryService expiryService;
    private final FileStorageService fileStorageService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;  // 비용이 높은 작업
    @Value("${server.default.user}")
    private String defaultUserProfilePic;
    @Value("${server.default.admin}")
    private String defaultAdminProfilePic;

    // 일반 사용자 저장
    public User saveUser(AddUserRequest dto) throws ResponseStatusException {
        // 이메일 중복 체크
        checkDuplicatedUser(dto.getEmail(), dto.getPhoneNumber());

        // 사용자 생성
        var user = User.builder().email(dto.getEmail()).password(bCryptPasswordEncoder.encode(dto.getPassword())).build();  // build the user first

        if (user.getCosmeticIds() == null) {
            user.setCosmeticIds(new HashSet<>());
        }

        // Add nickname, profileImage, phoneNumber only if they are not null
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getProfileImage() != null) {
            user.setProfileImage(dto.getProfileImage());
        } else {
            user.setProfileImage(defaultUserProfilePic);
        }
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber().replace("-", ""));
        }

        // 기본 권한 설정 ("ROLE_USER")
        user.addAuthority("ROLE_USER");
        return userRepository.save(user);
    }

    // 관리자 저장
    public String saveAdmin(AddUserRequest dto) {
        // 이메일 중복 체크
        checkDuplicatedUser(dto.getEmail(), dto.getPhoneNumber());

        // 관리자 생성
        var admin = User.builder().email(dto.getEmail()).password(bCryptPasswordEncoder.encode(dto.getPassword())).build();

        if (dto.getNickname() != null) {
            admin.setNickname(dto.getNickname());
        }
        if (dto.getProfileImage() != null) {
            admin.setProfileImage(dto.getProfileImage());
        } else {
            admin.setProfileImage(defaultAdminProfilePic);
        }
        if (dto.getPhoneNumber() != null) {
            admin.setPhoneNumber(dto.getPhoneNumber().replace("-", ""));
        }

        // 관리자 권한 추가
        admin.addAuthority("ROLE_ADMIN");
        return userRepository.save(admin).getId();
    }

    private void checkDuplicatedUser(String email, String phoneNumber) {
        List<String> errors = new ArrayList<>();

        userRepository.findByEmail(email)
                .ifPresent(u -> errors.add("이메일이 이미 사용 중입니다."));

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            userRepository.findByPhoneNumber(phoneNumber)
                    .ifPresent(u -> errors.add("전화번호가 이미 사용 중입니다."));
        }

        if (!errors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.join(" ", errors));
        }
    }

    // 사용자 ID로 조회
//    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
    public User findById(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    // 이메일로 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }

    // 닉네임으로 조회
    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname).orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
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
        return userRepository.findByEmailOrNickname(email, nickname).orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }

    public Optional<User> findUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    // 권한으로 사용자 목록 조회
    public List<User> findUsersByAuthority(String authority) {
        return userRepository.findByAuthority(authority);
    }

    // 이메일과 비밀번호로 사용자 조회 (로그인)
    public User findByEmailAndPassword(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password).orElseThrow(() -> new IllegalArgumentException("이메일 혹은 비밀번호가 틀립니다."));
    }

    public User addCosmeticById(String userId, String cosmeticId) {
        return userRepository.findById(userId).map(user -> {
            user.addCosmetic(cosmeticId);
            return userRepository.save(user);  // Save the updated user to the database
        }).orElseThrow(() -> new NoSuchElementException("No user found with id: " + userId));
    }

    public User removeCosmeticById(String userId, String cosmeticId) {
        return userRepository.findById(userId).map(user -> {
            user.removeCosmetic(cosmeticId);
            return userRepository.save(user);  // Save the updated user to the database
        }).orElseThrow(() -> new NoSuchElementException("No user found with id: " + userId));
    }

//    public User updateUser(User user, Map<String, Object> updates) {
//        if (updates.containsKey("nickname")) {
//            user.setNickname((String) updates.get("nickname"));
//        }
//        if (updates.containsKey("profileImage")) {
//            user.setProfileImage((String) updates.get("profileImage"));
//        }
//        return userRepository.save(user);
//    }


    /*
    Cascading
        If a User is deleted, consider what should happen to their Todo items.
        Should they be deleted as well, or should they remain in the database?
     */
    @Transactional
    public void deleteUserAndRelatedData(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Wrong user id."));

        // Delete all Todo entries related to the user
        todoRepository.deleteByUserId(new ObjectId(userId));

        // Delete all RefreshToken entries related to the user
        refreshTokenRepository.deleteByUserId(new ObjectId(userId));

        // Delete user profile picture from S3
        if (!user.getProfileImage().isEmpty()) {
            fileStorageService.deleteFile(user.getProfileImage());
        }

        // Delete all reviews made by the user and update cosmetics' scores
        for (var review : reviewService.findAllByUser(user)) {
            reviewService.deleteReview(user, review.getId());
        }

        // Delete all expiry items
        expiryService.deleteAllByUserId(userId);

        // Delete the User
        userRepository.deleteById(userId);

    }

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        PasswordResetToken token = tokenService.createPasswordResetToken(user);
        emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
    }

    public PasswordResetResponse requestPasswordResetByNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        PasswordResetToken token = tokenService.createPasswordResetToken(user);
        return new PasswordResetResponse(token, user);
    }


    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Token has expired");
        }

        User user = userRepository.findByEmail(resetToken.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }

}
