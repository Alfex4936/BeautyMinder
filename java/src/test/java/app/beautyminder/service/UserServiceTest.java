package app.beautyminder.service;

import app.beautyminder.domain.PasswordResetToken;
import app.beautyminder.domain.User;
import app.beautyminder.repository.PasswordResetTokenRepository;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.repository.TodoRepository;
import app.beautyminder.repository.UserRepository;
import app.beautyminder.service.auth.EmailService;
import app.beautyminder.service.auth.TokenService;
import app.beautyminder.service.auth.UserService;
import app.beautyminder.service.cosmetic.CosmeticExpiryService;
import app.beautyminder.service.review.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private TokenService tokenService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private CosmeticExpiryService expiryService;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setup() {
        userRepository = mock(UserRepository.class);
        todoRepository = mock(TodoRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        emailService = mock(EmailService.class);
        tokenService = mock(TokenService.class);
        reviewService = mock(ReviewService.class);
        expiryService = mock(CosmeticExpiryService.class);
        fileStorageService = mock(FileStorageService.class);
        bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
        userService = new UserService(
                userRepository, todoRepository, refreshTokenRepository, passwordResetTokenRepository, emailService, tokenService
                , reviewService, expiryService, fileStorageService, bCryptPasswordEncoder);
    }

    @Test
    public void testFindByNickname_Found() {
        User mockUser = User.builder().nickname("nickname").build();
        when(userRepository.findByNickname("nickname")).thenReturn(Optional.of(mockUser));

        User result = userService.findByNickname("nickname");
        assertEquals(mockUser, result);
    }

    @Test
    public void testFindByNickname_NotFound() {
        when(userRepository.findByNickname("nickname")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.findByNickname("nickname"));
    }

    @Test
    public void testFindUsersWithProfileImage() {
        User mockUser = User.builder().nickname("nickname").build();

        List<User> mockUsers = Collections.singletonList(mockUser);
        when(userRepository.findByProfileImageIsNotNull()).thenReturn(mockUsers);

        List<User> result = userService.findUsersWithProfileImage();
        assertEquals(mockUsers, result);
    }

    @Test
    public void testResetPassword_ValidToken() {
        PasswordResetToken resetToken = PasswordResetToken.builder().expiryDate(LocalDateTime.now().plusHours(1)).email("user@example.com").build();
        User mockUser = User.builder().build();
        mockUser.setPassword("oldPassword");

        when(passwordResetTokenRepository.findByToken("token")).thenReturn(Optional.of(resetToken));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(bCryptPasswordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        userService.resetPassword("token", "newPassword");

        verify(userRepository).save(mockUser);
        verify(passwordResetTokenRepository).delete(resetToken);
    }

    @Test
    public void testResetPassword_ExpiredToken() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setExpiryDate(LocalDateTime.now().minusHours(1));

        when(passwordResetTokenRepository.findByToken("token")).thenReturn(Optional.of(resetToken));

        assertThrows(IllegalArgumentException.class, () -> userService.resetPassword("token", "newPassword"));
    }

    @Test
    public void testUpdatePassword_Success() {
        User mockUser = User.builder().build();
        mockUser.setPassword("encodedCurrentPassword");
        when(userRepository.findById("userId")).thenReturn(Optional.of(mockUser));
        when(bCryptPasswordEncoder.matches("currentPassword", "encodedCurrentPassword")).thenReturn(true);
        when(bCryptPasswordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        userService.updatePassword("userId", "currentPassword", "newPassword");

        verify(userRepository).save(mockUser);
    }

    @Test
    public void testUpdatePassword_UserNotFound() {
        when(userRepository.findById("userId")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.updatePassword("userId", "currentPassword", "newPassword"));
    }

}
