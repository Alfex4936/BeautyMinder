package app.beautyminder.repository.elastic;

import app.beautyminder.domain.CosmeticMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;


@Repository
public interface CosmeticMetricRepository extends ElasticsearchRepository<CosmeticMetric, String> {

}