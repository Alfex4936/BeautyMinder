package app.beautyminder.repository;

import app.beautyminder.domain.Cosmetic;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface CosmeticRepository extends MongoRepository<Cosmetic, String> {

    List<Cosmetic> findByCategory(String category);

    @Query("{'expirationDate': {'$lte': ?0}}")
    List<Cosmetic> findExpiringSoon(LocalDate date);

    List<Cosmetic> findByPurchasedDate(LocalDate purchasedDate);

    List<Cosmetic> findByExpirationDate(LocalDate expirationDate);

    @NotNull Page<Cosmetic> findAll(@NotNull Pageable pageable);
}