package app.beautyminder.service;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.RefreshToken;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.User;
import app.beautyminder.repository.RefreshTokenRepository;
import app.beautyminder.repository.ReviewRepository;
import app.beautyminder.repository.elastic.EsReviewRepository;
import app.beautyminder.service.auth.RefreshTokenService;
import app.beautyminder.service.cosmetic.ReviewSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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