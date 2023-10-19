package app.beautyminder.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CosmeticMetricData {
    private String cosmeticId;
    private Long clickCount;
    private Long hitCount;
    private Long favCount;
}