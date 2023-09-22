package app.beautyminder.repository;

import app.beautyminder.domain.Cosmetic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CosmeticRepository extends MongoRepository<Cosmetic, String> {

    List<Cosmetic> findByUserId(String userId);

    List<Cosmetic> findByCategory(Cosmetic.Category category);

    List<Cosmetic> findByStatus(Cosmetic.Status status);

    @Query("{'expirationDate': {'$lte': ?0}}")
    List<Cosmetic> findExpiringSoon(LocalDate date);

    @Query("{'user.id': ?0, 'expirationDate': {'$lte': ?1}}")
    List<Cosmetic> findExpiringSoonByUserId(String userId, LocalDate date);

    Optional<Cosmetic> findByNameAndUserId(String name, String userId);

    List<Cosmetic> findByPurchasedDate(LocalDate purchasedDate);

    List<Cosmetic> findByExpirationDate(LocalDate expirationDate);
}