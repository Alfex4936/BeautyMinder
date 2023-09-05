package app.beautyminder.domain;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cosmetics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
public class Cosmetic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "purchased_date")
    private LocalDate purchasedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Cosmetic(String name, LocalDate expirationDate, LocalDate purchasedDate, Category category, Status status, User user) {
        this.name = name;
        this.expirationDate = expirationDate;
        this.purchasedDate = purchasedDate;
        this.category = category;
        this.status = status;
        this.user = user;
        this.createdDate = LocalDateTime.now();
    }

    public enum Category {
        스킨케어, 클렌징_필링, 마스크_팩, 선케어, 베이스, 아이, 립, 바디, 헤어, 네일, 향수, 기타
    }

    public enum Status {
        개봉, 미개봉
    }
}
