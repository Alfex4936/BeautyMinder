package app.beautyminder.service.cosmetic;

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

    public List<Cosmetic> getAllCosmetics() {
        return cosmeticRepository.findAll();
    }

    public Cosmetic createCosmetic(Cosmetic cosmetic) {
        return cosmeticRepository.save(cosmetic);
    }


    public Cosmetic updateCosmetic(Cosmetic cosmetic) {
        return cosmeticRepository.save(cosmetic);
    }


    public Cosmetic getCosmeticById(String id) {
        return cosmeticRepository.findById(id).orElse(null);
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

    public Cosmetic saveCosmetic(Cosmetic cosmetic) {
        return cosmeticRepository.save(cosmetic);
    }

    public Cosmetic updateCosmetic(String id, Cosmetic cosmeticDetails) {
        if (cosmeticRepository.existsById(id)) {
            return cosmeticRepository.save(cosmeticDetails);
        }
        return null;
    }

    public boolean deleteCosmetic(String id) {
        if (cosmeticRepository.existsById(id)) {
            cosmeticRepository.deleteById(id);
            return true;
        }
        return false;
    }
}