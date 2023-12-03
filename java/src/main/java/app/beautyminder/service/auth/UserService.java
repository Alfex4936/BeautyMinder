package app.beautyminder.service.auth;

import app.beautyminder.domain.PasscodeToken;
import app.beautyminder.domain.PasswordResetToken;
import app.beautyminder.domain.User;
import app.beautyminder.dto.PasswordResetResponse;
import app.beautyminder.dto.user.AddUserRequest;
import app.beautyminder.repository.PasswordResetTokenRepository;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.repository.TodoRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.FileStorageService;
import app.beautyminder.service.cosmetic.CosmeticExpiryService;
import app.beautyminder.service.review.ReviewService;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
        // Email duplicate check
        checkDuplicatedUser(dto.getEmail(), dto.getPhoneNumber());

        // User creation
        var user = buildUserFromRequest(dto, defaultUserProfilePic);
        user.addAuthority("ROLE_USER");

        return userRepository.save(user);
    }

    // 관리자 저장
    public User saveAdmin(AddUserRequest dto) {
        // Email duplicate check
        checkDuplicatedUser(dto.getEmail(), dto.getPhoneNumber());

        // Admin creation
        var admin = buildUserFromRequest(dto, defaultAdminProfilePic);
        admin.addAuthority("ROLE_ADMIN");

        return userRepository.save(admin);
    }

    private User buildUserFromRequest(AddUserRequest dto, String defaultProfilePic) {
        var user = User.builder()
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .build();

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        user.setProfileImage(dto.getProfileImage() != null ? dto.getProfileImage() : defaultProfilePic);
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber().replace("-", ""));
        }

        if (user.getCosmeticIds() == null) {
            user.setCosmeticIds(new HashSet<>());
        }

        return user;
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
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
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

    // 이메일과 비밀번호로 사용자 조회 (로그인)
    public User findByEmailAndPassword(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password).orElseThrow(() -> new IllegalArgumentException("이메일 혹은 비밀번호가 틀립니다."));
    }

    public User addCosmeticById(String userId, String cosmeticId) {
        return userRepository.findById(userId).map(user -> {
            user.addCosmetic(cosmeticId);
            return userRepository.save(user);  // Save the updated user to the database
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found with id: " + userId));
    }

    public User removeCosmeticById(String userId, String cosmeticId) {
        return userRepository.findById(userId).map(user -> {
            user.removeCosmetic(cosmeticId);
            return userRepository.save(user);  // Save the updated user to the database
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found with id: " + userId));
    }

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
        String userProfileImage = user.getProfileImage();
        if (userProfileImage != null && !userProfileImage.isEmpty() &&
                !List.of(defaultUserProfilePic, defaultAdminProfilePic).contains(userProfileImage)) {
            fileStorageService.deleteFile(userProfileImage);
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

    public PasscodeToken requestPassCode(String email) {
        PasscodeToken token = tokenService.createPasscode(email);
        emailService.sendVerificationEmail(email, token.getToken());
        return token;
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

    public void updatePassword(String userId, String currentPassword, String newPassword) {
        userRepository.findById(userId).ifPresentOrElse(
                user -> {
                    if (!bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect.");
                    } else if (bCryptPasswordEncoder.matches(newPassword, user.getPassword())) {
                        // If new password is the same as current password
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be the same as current password.");
                    }

                    // Update the password
                    user.setPassword(bCryptPasswordEncoder.encode(newPassword));
                    userRepository.save(user);
                },
                () -> {
                    // If there's no user associated with that ID, throw an exception
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user exists with the given ID.");
                }
        );
    }
}
