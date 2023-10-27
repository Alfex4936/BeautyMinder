package app.beautyminder.domain;

import com.mongodb.lang.Nullable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "cosmetic_expiries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public class CosmeticExpiry {

    @Id
    private String id; // MongoDB의 고유 식별자

    private String productName; // 화장품의 이름
    @Nullable
    private String brandName; // 화장품 브랜드 이름
    private LocalDate expiryDate; // 유통기한. OCR로 인식되거나 사용자가 선택할 수 있음.
    private boolean isExpiryRecognized = false; // OCR로 유통기한이 정상적으로 인식되었는지 여부
    @Nullable
    private String imageUrl; // 화장품 이미지 또는 유통기한이 표시된 부분의 사진 URL
    private LocalDate createdAt; // 정보가 생성된 날짜
    private LocalDate updatedAt; // 정보가 마지막으로 업데이트된 날짜
    private String userId; // 제품을 등록한 사용자의 ID
    @Nullable
    private String cosmeticId; // 검색 후 추가하는 방법
}
