package app.beautyminder.repository;

import app.beautyminder.domain.Todo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TodoRepository extends MongoRepository<Todo, String> {

    List<Todo> findByUserId(String userId);
    List<Todo> findByDate(LocalDate date);

    @Query("{'user.id': ?0, 'date': ?1}")
    List<Todo> findByUserIdAndDate(String userId, LocalDate date);

    @Query("{'date': {'$gte': ?0, '$lte': ?1}}")
    List<Todo> findBetweenDates(LocalDate startDate, LocalDate endDate);

    @Query("{'user.id': ?0, 'date': {'$gte': ?1, '$lte': ?2}}")
    List<Todo> findBetweenDatesByUserId(String userId, LocalDate startDate, LocalDate endDate);

    @Query("{'tasks': {'$regex': ?0, '$options': 'i'}}")
    List<Todo> findByTaskKeyword(String keyword);

    @Query("{'user.id': ?0, 'tasks': {'$regex': ?1, '$options': 'i'}}")
    List<Todo> findByTaskKeywordAndUserId(String userId, String keyword);

    void deleteByUserId(String userId);
}