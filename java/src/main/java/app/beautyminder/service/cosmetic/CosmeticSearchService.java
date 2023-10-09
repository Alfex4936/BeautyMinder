package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.EsCosmetic;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.elastic.EsCosmeticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CosmeticSearchService {

    private final CosmeticRepository cosmeticRepository;
    private final EsCosmeticRepository esCosmeticRepository;

    @Scheduled(cron = "0 */15 * * * ?") // every 15 mins
    public void indexCosmetics() {
        List<Cosmetic> cosmetics = cosmeticRepository.findAll();  // Fetch all cosmetics from MongoDB
        List<EsCosmetic> esCosmetics = cosmetics.stream()
                .map(this::convertToEsCosmetic)
                .collect(Collectors.toList());  // Convert to EsCosmetic objects
        esCosmeticRepository.saveAll(esCosmetics);  // Index all cosmetics to Elasticsearch
    }

    private EsCosmetic convertToEsCosmetic(Cosmetic cosmetic) {
        // Conversion logic
        return EsCosmetic.builder()
                .id(cosmetic.getId())
                .name(cosmetic.getName())
                .category(cosmetic.getCategory())
                .keywords(cosmetic.getKeywords())
                .build();
    }

}