package app.beautyminder.repository.elastic;

import app.beautyminder.domain.EsCosmetic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EsCosmeticRepository extends ElasticsearchRepository<EsCosmetic, String> {
    List<EsCosmetic> findByNameContaining(String name);

    List<EsCosmetic> findByCategory(String category);

    List<EsCosmetic> findByKeywordsContains(String keyword);
}