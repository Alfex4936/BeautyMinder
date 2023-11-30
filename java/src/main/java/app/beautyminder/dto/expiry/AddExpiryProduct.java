package app.beautyminder.dto.expiry;

import app.beautyminder.domain.TodoTask;
import com.mongodb.lang.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
public class AddExpiryProduct {
    private String productName; // 화장품의 이름
    @Nullable
    private String brandName; // 화장품 브랜드 이름
    private String expiryDate; // 유통기한. OCR로 인식되거나 사용자가 선택할 수 있음.
    private String openedDate; // 개봉 날짜
    private boolean expiryRecognized = false; // OCR로 유통기한이 정상적으로 인식되었는지 여부
    @Nullable
    private String imageUrl; // 화장품 이미지 또는 유통기한이 표시된 부분의 사진 URL
    @Nullable
    private String cosmeticId; // 검색 후 추가하는 방법
    private boolean opened; // 개봉 여부
}