package app.beautyminder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class BaumannSurveyAnswerDTO {
    @Schema(description = "A map of questions and choices",
            example = "{ \"A1\": 3, \"B2\": 4 }",
            implementation = Map.class)
    private Map<String, Integer> responses;
}