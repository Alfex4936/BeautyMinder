package app.beautyminder.service;

import app.beautyminder.config.error.exception.ArticleNotFoundException;
import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.Diary;
import app.beautyminder.dto.AddDiaryRequest;
import app.beautyminder.dto.UpdateDiaryRequest;
import app.beautyminder.repository.BlogRepository;
import app.beautyminder.repository.CosmeticRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CosmeticService {

    private final CosmeticRepository cosmeticRepository;

    // Create a new Cosmetic entry
    public Cosmetic createCosmetic(Cosmetic cosmetic) {
        return cosmeticRepository.save(cosmetic);
    }

    // Update an existing Cosmetic entry
    public Cosmetic updateCosmetic(Cosmetic cosmetic) {
        return cosmeticRepository.save(cosmetic);
    }

    // Delete a Cosmetic entry by ID
    public void deleteCosmetic(Long id) {
        cosmeticRepository.deleteById(id);
    }

    // Find all Cosmetics by user ID
    public List<Cosmetic> findCosmeticsByUserId(Long userId) {
        return cosmeticRepository.findByUserId(userId);
    }

    // Find all Cosmetics by category
    public List<Cosmetic> findCosmeticsByCategory(Cosmetic.Category category) {
        return cosmeticRepository.findByCategory(category);
    }

    // Find all Cosmetics by status
    public List<Cosmetic> findCosmeticsByStatus(Cosmetic.Status status) {
        return cosmeticRepository.findByStatus(status);
    }

    // Find Cosmetics expiring soon
    public List<Cosmetic> findCosmeticsExpiringSoon(LocalDate date) {
        return cosmeticRepository.findExpiringSoon(date);
    }

    // Find Cosmetics expiring soon by user ID
    public List<Cosmetic> findCosmeticsExpiringSoonByUserId(Long userId, LocalDate date) {
        return cosmeticRepository.findExpiringSoonByUserId(userId, date);
    }

    // Find Cosmetic by name and user ID
    public Optional<Cosmetic> findCosmeticByNameAndUserId(String name, Long userId) {
        return cosmeticRepository.findByNameAndUserId(name, userId);
    }

    // Find Cosmetics by purchased date
    public List<Cosmetic> findCosmeticsByPurchasedDate(LocalDate purchasedDate) {
        return cosmeticRepository.findByPurchasedDate(purchasedDate);
    }

    // Find Cosmetics by expiration date
    public List<Cosmetic> findCosmeticsByExpirationDate(LocalDate expirationDate) {
        return cosmeticRepository.findByExpirationDate(expirationDate);
    }
}