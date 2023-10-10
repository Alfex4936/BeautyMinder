package app.beautyminder.repository.elastic;

import app.beautyminder.domain.EsCosmetic;
import app.beautyminder.domain.EsReview;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EsReviewRepository extends ElasticsearchRepository<EsReview, String> {
    List<EsReview> findByContentContaining(String content);
}