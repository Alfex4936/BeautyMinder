package app.beautyminder.repository;

import app.beautyminder.domain.CosmeticExpiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CosmeticExpiryRepository extends MongoRepository<CosmeticExpiry, String> {
    List<CosmeticExpiry> findAllByUserIdOrderByExpiryDateAsc(String userId);
    Page<CosmeticExpiry> findAllByUserId(String userId, Pageable pageable);
    Optional<CosmeticExpiry> findByUserIdAndId(String userId, String expiryId);
    Optional<CosmeticExpiry> findByCosmeticId(String cosmeticId);
    List<CosmeticExpiry> findAllByUserIdAndExpiryDateBetween(String userId, LocalDate startDate, LocalDate endDate);
}