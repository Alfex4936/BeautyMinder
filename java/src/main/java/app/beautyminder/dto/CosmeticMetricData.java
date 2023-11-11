package app.beautyminder.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CosmeticMetricData {
    private String cosmeticId;
    private Long clickCount = 0L;
    private Long hitCount = 0L;
    private Long favCount = 0L;

    public void incrementClickCount() {
        if (clickCount == null) {
            this.clickCount = 0L;
        }
        this.clickCount++;
    }

    public void incrementHitCount() {
        if (hitCount == null) {
            this.hitCount = 0L;
        }
        this.hitCount++;
    }

    public void incrementFavCount() {
        if (favCount == null) {
            this.favCount = 0L;
        }
        this.favCount++;
    }
}