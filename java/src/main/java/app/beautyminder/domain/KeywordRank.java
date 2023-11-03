package app.beautyminder.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "keyword_ranks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class KeywordRank {

    @Id
    private String id;

    @Indexed
    private LocalDate date;

    @Builder.Default
    @Setter
    private List<String> rankings = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;
}