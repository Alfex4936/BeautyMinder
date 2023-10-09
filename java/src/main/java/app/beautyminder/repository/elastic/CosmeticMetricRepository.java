package app.beautyminder.repository.elastic;

import app.beautyminder.domain.CosmeticMetric;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CosmeticMetricRepository extends ElasticsearchRepository<CosmeticMetric, String> {
}