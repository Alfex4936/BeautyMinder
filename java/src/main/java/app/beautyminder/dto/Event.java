package app.beautyminder.dto;

import app.beautyminder.service.cosmetic.CosmeticRankService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Event {
    private String cosmeticId;
    private CosmeticRankService.ActionType type;

}