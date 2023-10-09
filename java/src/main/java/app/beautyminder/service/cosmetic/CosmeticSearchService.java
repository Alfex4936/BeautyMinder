package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.EsCosmetic;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.elastic.EsCosmeticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CosmeticSearchService {

    private final CosmeticRepository cosmeticRepository;
    private final EsCosmeticRepository esCosmeticRepository;
    private final RestHighLevelClient opensearchClient;

    @Scheduled(cron = "0 */15 * * * ?") // every 15 mins
    public void indexCosmetics() {
        log.info("Indexing all the cosmetics!!!");
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

    public String listAllIndices() throws IOException {
        Request request = new Request("GET", "/_cat/indices?v");
        Response response = opensearchClient.getLowLevelClient().performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    public String getIndexOfCosmetics() throws IOException {
        Request request = new Request("GET", "/cosmetics");
        Response response = opensearchClient.getLowLevelClient().performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    public String viewCosmeticsData() throws IOException {
        Request request = new Request("GET", "/cosmetics/_search");
        Response response = opensearchClient.getLowLevelClient().performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    public List<EsCosmetic> searchByName(String name) {
        return esCosmeticRepository.findByNameContaining(name);
    }

    public List<EsCosmetic> searchByCategory(Cosmetic.Category category) {
        return esCosmeticRepository.findByCategory(category);
    }

    public List<EsCosmetic> searchByKeyword(String keyword) {
        return esCosmeticRepository.findByKeywordsContains(keyword);
    }
}