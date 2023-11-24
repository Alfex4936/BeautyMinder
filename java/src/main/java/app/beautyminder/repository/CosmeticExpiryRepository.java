package app.beautyminder.repository;

import app.beautyminder.domain.CosmeticExpiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CosmeticExpiryRepository extends MongoRepository<CosmeticExpiry, String> {
    List<CosmeticExpiry> findAllByUserIdOrderByExpiryDateAsc(String userId);

    Page<CosmeticExpiry> findAllByUserId(String userId, Pageable pageable);

    Optional<CosmeticExpiry> findByUserIdAndId(String userId, String expiryId);

    Optional<CosmeticExpiry> findByCosmeticId(String cosmeticId);

    Optional<CosmeticExpiry> findByExpiryDate(String date);

    List<CosmeticExpiry> findAllByUserIdAndExpiryDateBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "{ 'userId': ?0 }", delete = true)
        // delete ALL
    void deleteAllByUserId(String userId);
}