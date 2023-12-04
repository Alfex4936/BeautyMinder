package app.beautyminder.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "baumanns")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BaumannTest {

    @Id
    private String id;

    @Indexed
    private LocalDate date;

    private String userId;

    @CreatedDate
    private LocalDateTime createdAt;

    private String baumannType; // "OSNT"

    private List<Integer> surveyAnswers; // Storing the survey answers [1, 2, 3, 4, ...]

    private Map<String, Double> baumannScores;
}