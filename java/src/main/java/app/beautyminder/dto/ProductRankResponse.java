package app.beautyminder.dto;

import app.beautyminder.domain.Cosmetic;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProductRankResponse {
    private List<Cosmetic> cosmetics;
    private LocalDateTime updatedAt;
}