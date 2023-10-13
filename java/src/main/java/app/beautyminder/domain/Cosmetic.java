package app.beautyminder.domain;

import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "cosmetics") // mongodb
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Cosmetic {

    private static final Logger LOG = LoggerFactory
            .getLogger(Cosmetic.class);

    @Id
    private String id;

    private String name;
    private String brand;

    @Setter
    private List<String> images = new ArrayList<>();

    private String glowpick_url;

    private LocalDate expirationDate;
    private LocalDateTime createdAt;
    private LocalDate purchasedDate;
    private String category;
    private Status status;

    private float averageRating = 0.0F; // ex) 3.14
    private int reviewCount = 0;
    private int totalRating = 0;

    @Builder.Default
    private List<String> keywords = new ArrayList<>();

//    @DBRef
//    private User user;

    @Builder
    public Cosmetic(String name, String brand, LocalDate expirationDate, LocalDate purchasedDate, String category, Status status) {
        this.name = name;
        this.brand = brand;
        this.expirationDate = expirationDate;
        this.purchasedDate = purchasedDate;
        this.category = category;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

//    public enum Category {
//        스킨케어, 클렌징_필링, 마스크_팩, 선케어, 베이스, 아이, 립, 바디, 헤어, 네일, 향수, 기타
//    }

    public enum Category {
        SKIN_TONER("스킨/토너"),
        LOTION_EMULSION("로션/에멀젼"),
        ESSENCE_SERUM("에센스/세럼"),
        CREAM("크림"),
        MIST("미스트"),
        EYELINER("아이라이너"),
        EYEBROW("아이브로우"),
        EYESHADOW("아이섀도우"),
        MASCARA("마스카라"),
        EYE_PRIMER("아이프라이머"),
        HIGHLIGHTER("하이라이터"),
        BLUSHER("블러셔"),
        SHADING("쉐딩"),
        CONTOURING_PALETTE("컨투어링팔레트"),
        BRUSH("브러시"),
        SPONGE_PUFF("스펀지/퍼프"),
        MAKEUP_ACCESSORIES("메이크업소품"),
        FACIAL_CLEANSER("페이셜클렌저"),
        MAKEUP_CLEANSER("메이크업클렌저"),
        POINT_REMOVER("포인트리무버"),
        EXFOLIATION_CARE("각질케어"),
        TANNING("태닝"),
        AFTER_SUN("애프터선"),
        BODY_LOTION_CREAM("바디로션/크림"),
        BODY_OIL_BALM("바디오일/밤"),
        BODY_MIST("바디미스트"),
        HAIR_STYLING("헤어스타일링"),
        HAIR_MAKEUP("헤어메이크업"),
        HAIR_ACCESSORIES("헤어소품"),
        POLISH("폴리시"),
        BASE_TOP_COAT("베이스/탑코트"),
        SKIN_HEALTH("피부건강"),
        DIET("다이어트"),
        HEALTH_FOOD("건강식품"),
        SKINCARE("스킨케어"),
        SUNCARE("선케어"),
        BODYCARE("바디케어"),
        CLEANSING("클렌징"),
        MAKEUP("메이크업"),
        HAIR("헤어"),
        BATH_AND_BODY("배쓰&바디"),
        FACE_OIL("페이스오일"),
        FOUNDATION("파운데이션"),
        FINISH_POWDER("피니시파우더"),
        CONCEALER("컨실러"),
        BASE_PRIMER("베이스/프라이머"),
        BB_CREAM("BB크림"),
        CC_CREAM("CC크림"),
        MAKEUP_FIXER("메이크업픽서"),
        LIPSTICK("립스틱"),
        LIP_TINT_LACQUER("립틴트/라커"),
        LIP_PENCIL("립펜슬"),
        LIP_GLOSS("립글로스"),
        LIP_CARE("립케어"),
        CLEANSING_TOOLS("클렌징도구"),
        SHEET_MASK("시트마스크"),
        FACE_MASK("페이스마스크"),
        PARTIAL_MASK("부분마스크"),
        MASSAGE_TOOLS("마사지도구"),
        SUN_CREAM("선크림"),
        SUN_CUSHION("선쿠션"),
        SUN_STICK("선스틱"),
        SUN_GEL("선젤"),
        SUN_BASE("선베이스"),
        SUN_SPRAY("선스프레이"),
        BODY_SLIMMING("바디슬리밍"),
        DEODORANT("데오드란트"),
        BODY_MASSAGE("바디마사지"),
        HAND_CARE("핸드케어"),
        FOOT_CARE("풋케어"),
        BATH_SHOWER("배쓰/샤워"),
        BODY_MAKEUP("바디메이크업"),
        HAIR_REMOVAL_PRODUCTS("제모용품"),
        SHAMPOO("샴푸"),
        RINSE_CONDITIONER("린스/컨디셔너"),
        TREATMENT_PACK("트리트먼트/팩"),
        HAIR_TONIC("헤어토닉"),
        DRY_SHAMPOO("드라이샴푸"),
        HAIR_CARE("헤어케어"),
        NAIL_STICKERS_TIPS("네일스티커/팁"),
        PEDICURE_STICKERS_TIPS("페디스티커/팁"),
        NAIL_CARE("네일케어"),
        NAIL_REMOVER("네일리무버"),
        NAIL_TOOLS("네일도구"),
        FEMININE_PRODUCTS("여성용품"),
        RELAXING("릴렉싱"),
        DENTAL_CARE("덴탈케어"),
        COLOR_LENS("컬러렌즈"),
        CLEAR_LENS("투명렌즈"),
        LENS_ACCESSORIES("렌즈용품"),
        PERFUME("향수"),
        AIR_FRESHENER("방향제"),
        BEAUTY_DEVICES("뷰티디바이스"),
        HAIR_DEVICES("헤어디바이스"),
        SHAVING("쉐이빙");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public static Category fromDisplayName(String displayName) {
            for (Category category : values()) {
                if (category.displayName.equals(displayName)) {
                    return category;
                }
            }
            throw new IllegalArgumentException("No enum constant with display name: " + displayName);
        }
    }

    public enum Status {
        개봉, 미개봉
    }

    public void updateAverageRating(int newRating) {
        this.reviewCount++;
        this.totalRating += newRating;
        this.averageRating = (float) this.totalRating / this.reviewCount;
        this.averageRating = Math.round(this.averageRating * 100.0) / 100.0f;  // Round to 2 decimal places
    }
}
