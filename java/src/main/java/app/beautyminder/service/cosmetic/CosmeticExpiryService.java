package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.CosmeticExpiry;
import app.beautyminder.domain.User;
import app.beautyminder.dto.expiry.AddExpiryProduct;
import app.beautyminder.repository.CosmeticExpiryRepository;
import app.beautyminder.service.MongoService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CosmeticExpiryService {

    private final CosmeticExpiryRepository cosmeticExpiryRepository;
    private final MongoService mongoService;

    public CosmeticExpiry createCosmeticExpiry(String userId, AddExpiryProduct cosmeticExpiryDTO) {
        CosmeticExpiry.CosmeticExpiryBuilder builder = CosmeticExpiry.builder();

        // Set values from DTO to builder, checking for null
        if (cosmeticExpiryDTO.getBrandName() != null) {
            builder.brandName(cosmeticExpiryDTO.getBrandName());
        }
        if (cosmeticExpiryDTO.getImageUrl() != null) {
            builder.imageUrl(cosmeticExpiryDTO.getImageUrl());
        }
        if (cosmeticExpiryDTO.getCosmeticId() != null) {
            builder.cosmeticId(cosmeticExpiryDTO.getCosmeticId());
        }

        // MUST
        builder.expiryDate(cosmeticExpiryDTO.getExpiryDate());
        builder.productName(cosmeticExpiryDTO.getProductName());
        builder.isExpiryRecognized(cosmeticExpiryDTO.isExpiryRecognized());
        builder.isOpened(cosmeticExpiryDTO.isOpened());
        builder.openedDate(cosmeticExpiryDTO.getOpenedDate());

        // Set the user ID from the User object
        builder.userId(userId);

        // Build the CosmeticExpiry object
        CosmeticExpiry cosmeticExpiry = builder.build();

        // Save to repository
        return cosmeticExpiryRepository.save(cosmeticExpiry);
    }

    public List<CosmeticExpiry> getAllCosmeticExpiriesByUserId(String userId) {
        return cosmeticExpiryRepository.findAllByUserIdOrderByExpiryDateAsc(userId);
    }

    public Page<CosmeticExpiry> getPagedAllCosmeticExpiriesByUserId(String userId, Pageable pageable) {
        return cosmeticExpiryRepository.findAllByUserId(userId, pageable);
    }

    public CosmeticExpiry getCosmeticExpiry(String userId, String expiryId) {
        Optional<CosmeticExpiry> existingExpiry = cosmeticExpiryRepository.findByUserIdAndId(userId, expiryId);
        if (existingExpiry.isPresent()) {
            return existingExpiry.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cosmetic expiry not found");
        }
    }

    public Optional<CosmeticExpiry> updateCosmeticExpiry(String expiryId, Map<String, Object> updates) {
        return mongoService.updateFields(expiryId, updates, CosmeticExpiry.class);
    }

    public void deleteCosmeticExpiry(String userId, String expiryId) {
        Optional<CosmeticExpiry> existingExpiry = cosmeticExpiryRepository.findByUserIdAndId(userId, expiryId);
        if (existingExpiry.isPresent()) {
            cosmeticExpiryRepository.deleteById(expiryId);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cosmetic expiry not found");
        }
    }

    public List<CosmeticExpiry> filterCosmeticExpiries(String userId, LocalDate startDate, LocalDate endDate) {
        return cosmeticExpiryRepository.findAllByUserIdAndExpiryDateBetween(userId, startDate, endDate);
    }

    public void deleteAllByUserId(String userId) {
        cosmeticExpiryRepository.deleteAllByUserId(userId);
    }

    public Optional<CosmeticExpiry> findByUserIdAndId(String userId, String expiryId) {
        return cosmeticExpiryRepository.findByUserIdAndId(userId, expiryId);
    }
}