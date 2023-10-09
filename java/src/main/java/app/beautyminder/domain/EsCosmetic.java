package app.beautyminder.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Document(indexName = "cosmetics") // elasticsearch
@Mapping(mappingPath = "elastic/cosmetic-mapping.json")
@Setting(settingPath = "elastic/cosmetic-setting.json")
public class EsCosmetic {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String brand;

    @Field(type = FieldType.Text)
    private String category;

    @Field(type = FieldType.Keyword)
    private List<String> keywords;
}