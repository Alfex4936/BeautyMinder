package app.beautyminder.service;

import app.beautyminder.domain.CosmeticExpiry;
import app.beautyminder.domain.Todo;
import app.beautyminder.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class MongoService {

    private final Map<Class<?>, Set<String>> bannedFieldsPerClass = new HashMap<>();
    /*
    MongoRepository:
        Provides CRUD operations and simple query derivation.
        Easier to use for common scenarios.
        Spring Data generates the implementation based on method names or annotations.

    MongoTemplate:
        More powerful, offering a wide range of MongoDB operations.
        Provides fine-grained control over queries and updates.
        Useful for complex queries, operations, or aggregations not covered by repository abstraction.
    */
    @Autowired
    private MongoTemplate mongoTemplate;

    public MongoService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        // Initialize the map with banned fields for each class
        bannedFieldsPerClass.put(Todo.class, Set.of("user"));
        bannedFieldsPerClass.put(User.class, Set.of("password", "email"));
        bannedFieldsPerClass.put(CosmeticExpiry.class, Set.of("id", "createdAt"));
    }

    public <T> Optional<T> updateFields(String id, Map<String, Object> updates, Class<T> entityClass) {
        Query query = new Query(Criteria.where("id").is(id));
        StringBuilder stringBuilder = new StringBuilder();

        Set<String> bannedFields = bannedFieldsPerClass.getOrDefault(entityClass, Collections.emptySet());

        if (mongoTemplate.exists(query, entityClass)) {
            Update update = new Update();
            updates.forEach((key, value) -> {
                if (!bannedFields.contains(key)) {
                    stringBuilder.append(key).append(",");
                    update.set(key, value);
                }
            });

            mongoTemplate.updateFirst(query, update, entityClass);
            log.info("BEMINDER: {} has been updated: {}", entityClass.getSimpleName(), stringBuilder);
            return Optional.ofNullable(mongoTemplate.findOne(query, entityClass));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, entityClass.getSimpleName() + " not found: " + id);
        }
    }
}
