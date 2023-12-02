package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.CosmeticExpiry;
import app.beautyminder.dto.expiry.AddExpiryProduct;
import app.beautyminder.repository.CosmeticExpiryRepository;
import app.beautyminder.service.MongoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class CosmeticExpiryServiceTest {

    private final String userId = "expiryTestUser";
    private CosmeticExpiryRepository mockCosmeticRepository;
    private CosmeticExpiryService cosmeticExpiryService;

    private AddExpiryProduct createDTO() {
        AddExpiryProduct dto = new AddExpiryProduct();
        dto.setBrandName("Brand");
        dto.setImageUrl("http://example.com/image.jpg");
        dto.setCosmeticId("cosmetic123");
        dto.setExpiryDate("2023-12-31");
        dto.setOpenedDate("2023-01-01");
        dto.setProductName("Product");
        dto.setExpiryRecognized(true);
        dto.setOpened(true);

        return dto;
    }

    private CosmeticExpiry createExpiry() {
        var dto = createDTO();

        return CosmeticExpiry.builder().cosmeticId(dto.getCosmeticId()).expiryRecognized(false).opened(false).build();
    }

    @BeforeEach
    void setUp() {
        mockCosmeticRepository = mock(CosmeticExpiryRepository.class);
        MongoService mockMongoService = mock(MongoService.class);
        cosmeticExpiryService = new CosmeticExpiryService(mockCosmeticRepository, mockMongoService);
    }

    @Test
    void whenCreateCosmeticExpiry_thenSaveAndReturnCosmeticExpiry() {
        var dto = createDTO();

        CosmeticExpiry expectedExpiry = CosmeticExpiry.builder()
                .userId(userId)
                .brandName("Brand")
                .expiryRecognized(false).opened(true).build();


        when(mockCosmeticRepository.save(any(CosmeticExpiry.class))).thenReturn(expectedExpiry);

        // Act
        CosmeticExpiry result = cosmeticExpiryService.createCosmeticExpiry(userId, dto);

        // Assert
        assertEquals(expectedExpiry, result);
        verify(mockCosmeticRepository).save(any(CosmeticExpiry.class));
    }

    @Test
    void whenGetAllCosmeticExpiriesByUserId_thenReturnListOfExpiries() {
        List<CosmeticExpiry> expectedList = Arrays.asList(
                createExpiry(),
                createExpiry()
        );

        when(mockCosmeticRepository.findAllByUserIdOrderByExpiryDateAsc(userId)).thenReturn(expectedList);

        // Act
        List<CosmeticExpiry> result = cosmeticExpiryService.getAllCosmeticExpiriesByUserId(userId);

        // Assert
        assertEquals(expectedList, result);
        verify(mockCosmeticRepository).findAllByUserIdOrderByExpiryDateAsc(userId);
    }

    @Test
    void whenGetCosmeticExpiry_thenReturnExpiry() {
        String expiryId = "expiry123";
        CosmeticExpiry expectedExpiry = createExpiry();

        when(mockCosmeticRepository.findByUserIdAndId(userId, expiryId)).thenReturn(Optional.of(expectedExpiry));

        // Act
        CosmeticExpiry result = cosmeticExpiryService.getCosmeticExpiry(userId, expiryId);

        // Assert
        assertEquals(expectedExpiry, result);
    }

    @Test
    void whenGetCosmeticExpiry_andExpiryNotFound_thenThrowException() {
        String expiryId = "nonExistingId";

        when(mockCosmeticRepository.findByUserIdAndId(userId, expiryId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResponseStatusException.class, () -> {
            cosmeticExpiryService.getCosmeticExpiry(userId, expiryId);
        });
    }

    @Test
    void whenDeleteCosmeticExpiry_thenDeleteTheExpiry() {
        String expiryId = "expiry123";
        CosmeticExpiry existingExpiry = createExpiry();

        when(mockCosmeticRepository.findByUserIdAndId(userId, expiryId)).thenReturn(Optional.of(existingExpiry));

        // Act
        cosmeticExpiryService.deleteCosmeticExpiry(userId, expiryId);

        // Assert
        verify(mockCosmeticRepository).deleteById(expiryId);
    }

    @Test
    void whenDeleteCosmeticExpiry_andExpiryNotFound_thenThrowException() {
        String expiryId = "nonExistingId";

        when(mockCosmeticRepository.findByUserIdAndId(userId, expiryId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResponseStatusException.class, () -> {
            cosmeticExpiryService.deleteCosmeticExpiry(userId, expiryId);
        });
    }

}