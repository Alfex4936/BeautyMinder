package app.beautyminder.service.cosmetic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.ActionListener;
import org.opensearch.action.DocWriteRequest;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.script.Script;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CosmeticMetricService {

    private final RestHighLevelClient opensearchClient;

    private final List<DocWriteRequest<?>> bulkRequests = new ArrayList<>();

    public void incrementClickCount(String cosmeticId) {
        UpdateRequest updateRequest = new UpdateRequest("cosmetic_metrics", cosmeticId)
                .script(new Script("ctx._source.clickCounts += 1"))
                .retryOnConflict(3);  // Retry up to 3 times if there are version conflicts
        synchronized (bulkRequests) {
            bulkRequests.add(updateRequest);
        }
    }

    public void incrementHitCount(String cosmeticId) {
        UpdateRequest updateRequest = new UpdateRequest("cosmetic_metrics", cosmeticId)
                .script(new Script("ctx._source.hitCounts += 1"))
                .retryOnConflict(3);  // Retry up to 3 times if there are version conflicts
        synchronized (bulkRequests) {
            bulkRequests.add(updateRequest);
        }
    }

    @Scheduled(cron = "0 */10 * * * ?") // every 1 min
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
}