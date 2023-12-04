package app.beautyminder.service;

import app.beautyminder.domain.RefreshToken;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.service.auth.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private RefreshToken createToken() {
        return RefreshToken.builder().refreshToken("newToken").build();
    }

    @Test
    public void testFindByRefreshToken_Found() {
        RefreshToken mockToken = createToken();
        when(refreshTokenRepository.findByRefreshToken(anyString())).thenReturn(Optional.of(mockToken));

        RefreshToken result = refreshTokenService.findByRefreshToken("token");
        assertEquals(mockToken, result);
    }

    @Test
    public void testFindByRefreshToken_NotFound() {
        when(refreshTokenRepository.findByRefreshToken(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            refreshTokenService.findByRefreshToken("invalidToken");
        });
    }

    @Test
    public void testUpdateRefreshTokenByUserId() {
        List<RefreshToken> mockTokens = List.of(createToken(), createToken()); // Create mock RefreshToken objects
        when(refreshTokenRepository.findAllByUserId(anyString())).thenReturn(mockTokens);

        refreshTokenService.updateRefreshTokenByUserId("userId", "newToken");

        for (RefreshToken token : mockTokens) {
            assertEquals("newToken", token.getRefreshToken()); // Assuming getToken() method exists
        }
        verify(refreshTokenRepository).saveAll(mockTokens);
    }

    @Test
    public void testUpdateRefreshTokenByUserId_NoTokens() {
        when(refreshTokenRepository.findAllByUserId(anyString())).thenReturn(List.of());

        refreshTokenService.updateRefreshTokenByUserId("userId", "newToken");

        verify(refreshTokenRepository, times(1)).saveAll(anyList());
    }
}