package app.beautyminder.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Document(collection = "cosmetic_expiries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CosmeticExpiry {

    @Id
    private String id; // MongoDB의 고유 식별자

    private String productName; // 화장품의 이름
    private String brandName; // 화장품 브랜드 이름
    private LocalDate expiryDate; // 유통기한. OCR로 인식되거나 사용자가 선택할 수 있음.
    private boolean isExpiryRecognized; // OCR로 유통기한이 정상적으로 인식되었는지 여부
    private String imageUrl; // 화장품 이미지 또는 유통기한이 표시된 부분의 사진 URL
    private LocalDate createdAt; // 정보가 생성된 날짜
    private LocalDate updatedAt; // 정보가 마지막으로 업데이트된 날짜
    private String userId; // 제품을 등록한 사용자의 ID
}
