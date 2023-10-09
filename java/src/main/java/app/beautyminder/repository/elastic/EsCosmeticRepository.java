package app.beautyminder.repository.elastic;

import app.beautyminder.domain.EsCosmetic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EsCosmeticRepository extends ElasticsearchRepository<EsCosmetic, String> {


}