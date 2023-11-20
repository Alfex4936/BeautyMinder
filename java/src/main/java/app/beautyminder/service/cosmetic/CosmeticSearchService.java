package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.EsCosmetic;
import app.beautyminder.domain.EsReview;
import app.beautyminder.repository.CosmeticRepository;
import app.beautyminder.repository.elastic.EsCosmeticRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.Request;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Response;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class CosmeticSearchService {

    private final CosmeticRepository cosmeticRepository;
    private final EsCosmeticRepository esCosmeticRepository;
    private final RestHighLevelClient opensearchClient;
    private final ObjectMapper objectMapper;

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
        var boolQueryBuilder = QueryBuilders.boolQuery()
                .should(QueryBuilders.matchPhrasePrefixQuery("name", name).boost(2))
                .should(QueryBuilders.matchQuery("name", name));

        var searchRequest = new SearchRequest("cosmetics");
        var searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.minScore(5f); // min score 5
        searchRequest.source(searchSourceBuilder);

        try {
            var searchResponse = opensearchClient.search(searchRequest, RequestOptions.DEFAULT);
            var hits = searchResponse.getHits();

            return Stream.of(hits.getHits())
                    .map(SearchHit::getSourceAsString)
                    .map(this::convertJsonToEsCosmetic).filter(Objects::nonNull)
                    .map(EsCosmetic::getId)
                    .map(cosmeticRepository::findById)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Exception occurred while searching reviews: ", e);
            return Collections.emptyList();
        }

//        List<EsCosmetic> esCosmetics = esCosmeticRepository.findByNameContaining(name);
//        return convertEsCosmeticToCosmetic(esCosmetics);
    }

    public List<Cosmetic> searchByCategory(String category) {
        Pageable pageable = PageRequest.of(0, 10); // Page number 0, max 10 results

        List<EsCosmetic> esCosmetics = esCosmeticRepository.findByCategory(category, pageable);
        return convertEsCosmeticToCosmetic(esCosmetics);
    }

    public List<Cosmetic> searchByKeyword(String keyword) {
        Set<EsCosmetic> multipleQueries = new HashSet<>();
        Pageable pageable = PageRequest.of(0, 5); // Page number 0, max 5 results

        for (var word : keyword.split(" ")) {
            multipleQueries.addAll(esCosmeticRepository.findByKeywordsContains(word, pageable));
        }
        return convertEsCosmeticToCosmetic(multipleQueries.stream().toList());
    }

    private List<Cosmetic> convertEsCosmeticToCosmetic(List<EsCosmetic> esCosmetics) {
        // Extract IDs from EsCosmetic list
        List<String> ids = esCosmetics.stream()
                .map(EsCosmetic::getId)
                .collect(Collectors.toList());

        // Fetch and return Cosmetic objects based on the extracted IDs
        return cosmeticRepository.findAllById(ids);
    }

    private EsCosmetic convertJsonToEsCosmetic(String json) {
        try {
            return objectMapper.readValue(json, EsCosmetic.class);
        } catch (IOException e) {
            log.error("Failed to deserialize JSON to EsReview: ", e);
            return null;
        }
    }
}