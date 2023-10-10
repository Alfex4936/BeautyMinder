package app.beautyminder.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Document(indexName = "reviews") // elasticsearch
@Mapping(mappingPath = "elastic/review-mapping.json")
@Setting(settingPath = "elastic/cosmetic-setting.json")
public class EsReview {
    @Id
    private String id; // will be Review mongodb id

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Integer)
    private int rating;

    @Field(type = FieldType.Text)
    private String userName;

    @Field(type = FieldType.Text)
    private String cosmeticName;
}