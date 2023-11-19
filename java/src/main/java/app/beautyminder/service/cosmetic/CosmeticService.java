package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.repository.CosmeticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CosmeticService {

    @Value("${server.default.cosmetic}")
    private String defaultCosmeticPic;
    private final MongoTemplate mongoTemplate;

    private final CosmeticRepository cosmeticRepository;

    public Optional<Cosmetic> findById(String cosmeticId) {
        return cosmeticRepository.findById(cosmeticId);
    }

    public List<Cosmetic> getAllCosmetics() {
        return cosmeticRepository.findAll();
    }

    public Page<Cosmetic> getAllCosmeticsInPage(Pageable pageable) {
        return cosmeticRepository.findAll(pageable);
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
        if (cosmetic.getImages().isEmpty()) {
            cosmetic.addImage(defaultCosmeticPic);
        }
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

    public Cosmetic getRandomCosmetic() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.sample(1)
        );

        AggregationResults<Cosmetic> results = mongoTemplate.aggregate(
                aggregation, Cosmetic.class, Cosmetic.class
        );

        List<Cosmetic> randomCosmetics = results.getMappedResults();

        if (!randomCosmetics.isEmpty()) {
            return randomCosmetics.get(0);
        } else {
            return null; // or handle the case where no cosmetics are available
        }
    }

}