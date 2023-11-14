package app.beautyminder.service;

import app.beautyminder.domain.CosmeticExpiry;
import app.beautyminder.domain.Todo;
import app.beautyminder.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
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

    private boolean isValidField(Class<?> entityClass, String fieldName) {
        Set<String> fields = new HashSet<>();
        Field[] allFields = entityClass.getDeclaredFields();
        for (Field field : allFields) {
            fields.add(field.getName());
        }
        return fields.contains(fieldName);
    }

    public <T> Optional<T> updateFields(String id, Map<String, Object> updates, Class<T> entityClass) {
        var query = new Query(Criteria.where("id").is(id));
        var stringBuilder = new StringBuilder();

        var bannedFields = bannedFieldsPerClass.getOrDefault(entityClass, Set.of());

        if (mongoTemplate.exists(query, entityClass)) {
            var update = new Update();
            updates.entrySet().stream()
                    .filter(entry -> !bannedFields.contains(entry.getKey()))
                    .filter(entry -> isValidField(entityClass, entry.getKey()))
                    .forEach(entry -> {
                        stringBuilder.append(entry.getKey()).append(",");
                        update.set(entry.getKey(), entry.getValue());
                    });

            mongoTemplate.updateFirst(query, update, entityClass);
            log.info("BEMINDER: {} has been updated: {}", entityClass.getSimpleName(), stringBuilder);
            return Optional.ofNullable(mongoTemplate.findOne(query, entityClass));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, entityClass.getSimpleName() + " not found: " + id);
        }
    }

    public <T> boolean existsWithReference(Class<T> entityClass, String entityId, String referenceFieldName, String referenceId) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(entityId))
                .and(referenceFieldName + ".$id").is(new ObjectId(referenceId)));
        query.fields().position("program", 1);
        T application = mongoTemplate.findOne(query, entityClass);
        log.error("Check: {}", application);

        return mongoTemplate.exists(query, entityClass);
    }

    public <T> boolean touch(Class<T> entitiyClass, String id, String field) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().currentDate(field);
        var updateResult = mongoTemplate.updateFirst(query, update, entitiyClass);

        return updateResult.wasAcknowledged();
    }
}
