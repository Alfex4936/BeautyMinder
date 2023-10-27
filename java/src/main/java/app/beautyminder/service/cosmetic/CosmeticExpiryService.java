package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.CosmeticExpiry;
import app.beautyminder.repository.CosmeticExpiryRepository;
import app.beautyminder.repository.CosmeticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CosmeticExpiryService {

    private final CosmeticExpiryRepository cosmeticExpiryRepository;
    public CosmeticExpiry createCosmeticExpiry(CosmeticExpiry cosmeticExpiry) {
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

    public CosmeticExpiry updateCosmeticExpiry(String userId, String expiryId, CosmeticExpiry updatedExpiry) {
        Optional<CosmeticExpiry> existingExpiry = cosmeticExpiryRepository.findByUserIdAndId(userId, expiryId);
        if (existingExpiry.isPresent()) {
            updatedExpiry.setId(expiryId);
            return cosmeticExpiryRepository.save(updatedExpiry);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cosmetic expiry not found");
        }
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
}