package app.beautyminder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class BaumannTypeDTO {
    private String skinType;
    private Map<String, Double> scores;
    private Map<String, Integer> metadata;
}