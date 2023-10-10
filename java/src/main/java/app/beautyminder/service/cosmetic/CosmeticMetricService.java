package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.domain.CosmeticMetric;
import app.beautyminder.repository.CosmeticRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.ActionListener;
import org.opensearch.action.DocWriteRequest;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.script.Script;
import org.opensearch.script.ScriptType;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.ScriptSortBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CosmeticMetricService {

    private final RestHighLevelClient opensearchClient;
    private final CosmeticRepository cosmeticRepository;

    private final List<DocWriteRequest<?>> bulkRequests = new ArrayList<>();

    // clicks might be considered more valuable as they represent a user taking a clear action based on their interest
    private final double CLICK_WEIGHT = 1.8;
    private final double HIT_WEIGHT = 1.1;

    public void incrementClickCount(String cosmeticId) { // upsert
        Map<String, Object> parameters = Collections.singletonMap("count", 1);
        Script inline = new Script(ScriptType.INLINE, "painless",
                "ctx._source.clickCounts += params.count", parameters);
        UpdateRequest updateRequest = new UpdateRequest("cosmetic_metrics", cosmeticId)
                .script(inline)
                .upsert(new IndexRequest().index("cosmetic_metrics").id(cosmeticId)
                        .source("clickCounts", 1, "hitCounts", 0))  // initial values for upsert
                .retryOnConflict(3);  // Retry up to 3 times if there are version conflicts
        synchronized (bulkRequests) {
            bulkRequests.add(updateRequest);
        }
    }

    public void incrementHitCount(String cosmeticId) {
        Map<String, Object> parameters = Collections.singletonMap("count", 1);
        Script inline = new Script(ScriptType.INLINE, "painless",
                "ctx._source.hitCounts += params.count", parameters);
        UpdateRequest updateRequest = new UpdateRequest("cosmetic_metrics", cosmeticId)
                .script(inline)
                .upsert(new IndexRequest().index("cosmetic_metrics").id(cosmeticId)
                        .source("clickCounts", 0, "hitCounts", 1))  // initial values for upsert
                .retryOnConflict(3);  // Retry up to 3 times if there are version conflicts
        synchronized (bulkRequests) {
            bulkRequests.add(updateRequest);
        }
    }

    @Scheduled(cron = "0 */10 * * * ?") // every 10 min
    public void executeBulkUpdates() {
        List<DocWriteRequest<?>> currentBulkRequests;
        synchronized (bulkRequests) {
            currentBulkRequests = new ArrayList<>(bulkRequests);
            bulkRequests.clear();
        }
        if (!currentBulkRequests.isEmpty()) {
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.add(currentBulkRequests);
            opensearchClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                @Override
                public void onResponse(BulkResponse bulkResponse) {
                    if (bulkResponse.hasFailures()) {
                        log.error(bulkResponse.buildFailureMessage());
                        requeueFailedRequests(bulkResponse, currentBulkRequests);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    log.error(e.getMessage());
                }
            });
        }

        log.info("!!! Bulked all the metrics!!!");
    }

    private void requeueFailedRequests(BulkResponse bulkResponse, List<DocWriteRequest<?>> currentBulkRequests) {
        int itemIndex = 0;
        for (BulkItemResponse itemResponse : bulkResponse) {
            if (itemResponse.isFailed()) {
                synchronized (bulkRequests) {
                    bulkRequests.add(currentBulkRequests.get(itemIndex));
                }
            }
            itemIndex++;
        }
    }

    public List<Cosmetic> getTopRankedCosmetics(int size) {
        // Create a search request for the cosmetic_metrics index
        SearchRequest searchRequest = new SearchRequest("cosmetic_metrics");

        // Build the search query
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(size);  // Limit the number of results

        // Create a map to hold the script parameters
        Map<String, Object> scriptParams = Map.of(
                "clickWeight", CLICK_WEIGHT,
                "hitWeight", HIT_WEIGHT
        );

        // Define a script to calculate the score based on both clickCounts and hitCounts with weights
        Script script = new Script(ScriptType.INLINE, "painless",
                "doc['clickCounts'].value * params.clickWeight + doc['hitCounts'].value * params.hitWeight",
                scriptParams);

        // Use a script-based sorting to sort the documents based on the script's output
        searchSourceBuilder.sort(new ScriptSortBuilder(script, ScriptSortBuilder.ScriptSortType.NUMBER).order(SortOrder.DESC));
        searchRequest.source(searchSourceBuilder);

        try {
            // Execute the search request
            SearchResponse searchResponse = opensearchClient.search(searchRequest, RequestOptions.DEFAULT);
            // Convert the search hits to a list of CosmeticMetric objects
            return Arrays.stream(searchResponse.getHits().getHits())
                    .map(hit -> {
                        try {
                            CosmeticMetric cosmeticMetric = new ObjectMapper().readValue(hit.getSourceAsString(), CosmeticMetric.class);
                            cosmeticMetric.setId(hit.getId());  // Set the document ID on the CosmeticMetric object
                            Optional<Cosmetic> optCosmetic = cosmeticRepository.findById(hit.getId());
                            if (optCosmetic.isPresent()) {
                                return optCosmetic.get();
                            } else {
                                log.warn("No Cosmetic found for id: {}", hit.getId());
                                return null;
                            }
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(Objects::nonNull)  // Remove any nulls resulting from missing Cosmetics
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Exception occurred while fetching top ranked cosmetics: ", e);
            throw new RuntimeException(e);
        }
    }
}