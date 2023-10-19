package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.repository.CosmeticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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

    public List<Cosmetic> findCosmeticsByCategory(String category) {
        return cosmeticRepository.findByCategory(category);
    }


    public List<Cosmetic> findCosmeticsExpiringSoon(LocalDate date) {
        return cosmeticRepository.findExpiringSoon(date);
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