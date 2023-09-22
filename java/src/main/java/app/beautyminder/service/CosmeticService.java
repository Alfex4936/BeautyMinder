package app.beautyminder.service;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.repository.CosmeticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CosmeticService {

    private final CosmeticRepository cosmeticRepository;

    public Cosmetic createCosmetic(Cosmetic cosmetic) {
        return cosmeticRepository.save(cosmetic);
    }

    public Cosmetic updateCosmetic(Cosmetic cosmetic) {
        return cosmeticRepository.save(cosmetic);
    }

    public void deleteCosmetic(String id) {
        cosmeticRepository.deleteById(id);
    }

    public List<Cosmetic> findCosmeticsByUserId(String userId) {
        return cosmeticRepository.findByUserId(userId);
    }

    public List<Cosmetic> findCosmeticsByCategory(Cosmetic.Category category) {
        return cosmeticRepository.findByCategory(category);
    }

    public List<Cosmetic> findCosmeticsByStatus(Cosmetic.Status status) {
        return cosmeticRepository.findByStatus(status);
    }

    public List<Cosmetic> findCosmeticsExpiringSoon(LocalDate date) {
        return cosmeticRepository.findExpiringSoon(date);
    }

    public List<Cosmetic> findCosmeticsExpiringSoonByUserId(String userId, LocalDate date) {
        return cosmeticRepository.findExpiringSoonByUserId(userId, date);
    }

    public Optional<Cosmetic> findCosmeticByNameAndUserId(String name, String userId) {
        return cosmeticRepository.findByNameAndUserId(name, userId);
    }

    public List<Cosmetic> findCosmeticsByPurchasedDate(LocalDate purchasedDate) {
        return cosmeticRepository.findByPurchasedDate(purchasedDate);
    }

    public List<Cosmetic> findCosmeticsByExpirationDate(LocalDate expirationDate) {
        return cosmeticRepository.findByExpirationDate(expirationDate);
    }
}