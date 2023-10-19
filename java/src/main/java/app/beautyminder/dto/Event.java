package app.beautyminder.dto;

import app.beautyminder.service.cosmetic.CosmeticMetricService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Event {
    private String cosmeticId;
    private CosmeticMetricService.ActionType type;

}