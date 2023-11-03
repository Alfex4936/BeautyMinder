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

    //    @PostConstruct
    @Scheduled(cron = "0 0 4 * * ?", zone = "Asia/Seoul") // everyday 4am
    public void indexCosmetics() {
        List<Cosmetic> cosmetics = cosmeticRepository.findAll();  // Fetch all cosmetics from MongoDB
        List<EsCosmetic> esCosmetics = cosmetics.stream()
                .map(this::convertToEsCosmetic)
                .collect(Collectors.toList());  // Convert to EsCosmetic objects
        esCosmeticRepository.saveAll(esCosmetics);  // Index all cosmetics to Elasticsearch
        log.info(">>> Indexed all the cosmetics!!!");
    }

    private EsCosmetic convertToEsCosmetic(Cosmetic cosmetic) {
        // Conversion logic
        return EsCosmetic.builder()
                .id(cosmetic.getId())
                .name(cosmetic.getName())
                .brand(cosmetic.getBrand())
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

    public String viewCosmeticMetricsData() throws IOException {
        Request request = new Request("GET", "/cosmetic_metrics");
        Response response = opensearchClient.getLowLevelClient().performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    public void delete() {
        try {
            Request request = new Request("DELETE", "/cosmetics");
            Response response = opensearchClient.getLowLevelClient().performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                log.error("Failed to delete all indices: " + EntityUtils.toString(response.getEntity()));
            } else {
                log.info("Successfully deleted all indices in the 'cosmetics' index.");
            }
        } catch (IOException e) {
            log.error("Exception occurred while deleting all indices: ", e);
        }
    }


    public List<Cosmetic> searchByName(String name) {
        List<EsCosmetic> esCosmetics = esCosmeticRepository.findByNameContaining(name);
        return convertEsCosmeticToCosmetic(esCosmetics);
    }

    public List<Cosmetic> searchByCategory(String category) {
        List<EsCosmetic> esCosmetics = esCosmeticRepository.findByCategory(category);
        return convertEsCosmeticToCosmetic(esCosmetics);
    }

    public List<Cosmetic> searchByKeyword(String keyword) {
        List<EsCosmetic> esCosmetics = esCosmeticRepository.findByKeywordsContains(keyword);
        return convertEsCosmeticToCosmetic(esCosmetics);
    }

    private List<Cosmetic> convertEsCosmeticToCosmetic(List<EsCosmetic> esCosmetics) {
        // Extract IDs from EsCosmetic list
        List<String> ids = esCosmetics.stream()
                .map(EsCosmetic::getId)
                .collect(Collectors.toList());

        // Fetch and return Cosmetic objects based on the extracted IDs
        return cosmeticRepository.findAllById(ids);
    }
}