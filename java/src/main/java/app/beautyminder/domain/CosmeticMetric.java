package app.beautyminder.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;

@Document(indexName = "cosmetic_metrics") // elasticsearch
@Mapping(mappingPath = "elastic/cosmeticmetric-mapping.json")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosmeticMetric {

    @Id
    private String id;

//    private String cosmeticId; // 위 id가 이미 cosmeticId로 지정될거임

    @Field(type = FieldType.Integer)
    private int clickCounts;
    @Field(type = FieldType.Integer)
    private int hitCounts;
}