package app.beautyminder.service;

import app.beautyminder.domain.CosmeticExpiry;
import app.beautyminder.domain.Review;
import app.beautyminder.domain.Todo;
import app.beautyminder.domain.User;
import jakarta.annotation.PostConstruct;
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
import java.util.stream.Collectors;

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
    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void initClass() {
        // Initialize the map with banned fields for each class
        bannedFieldsPerClass.put(Todo.class, Set.of("id", "user", "createdAt"));
        bannedFieldsPerClass.put(User.class, Set.of("id", "password", "email", "createdAt"));
//        bannedFieldsPerClass.put(Review.class, Set.of("isFiltered", "nlpAnalysis"));
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
        var query = new Query(Criteria.where("_id").is(new ObjectId(id)));
        if (!mongoTemplate.exists(query, entityClass)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, entityClass.getSimpleName() + " not found: " + id);
        }

        var update = createUpdateOperation(updates, entityClass);
        if (update.getUpdateObject().isEmpty()) {
            return Optional.ofNullable(mongoTemplate.findOne(query, entityClass));
        }

        mongoTemplate.updateFirst(query, update, entityClass);
        logUpdatedFields(entityClass, update);

        return Optional.ofNullable(mongoTemplate.findOne(query, entityClass));
    }

    private <T> Update createUpdateOperation(Map<String, Object> updates, Class<T> entityClass) {
        var bannedFields = bannedFieldsPerClass.getOrDefault(entityClass, Set.of());
        var update = new Update();

        updates.entrySet().stream()
                .filter(entry -> !bannedFields.contains(entry.getKey()))
                .filter(entry -> isValidField(entityClass, entry.getKey()))
                .filter(this::isValidUpdate)
                .forEach(entry -> update.set(entry.getKey(), entry.getValue()));

        return update;
    }

    private boolean isValidUpdate(Map.Entry<String, Object> entry) {
        return switch (entry.getKey()) {
            case "profileImage" -> isValidProfileImage((String) entry.getValue());
            case "phoneNumber" -> isValidKoreanPhoneNumber((String) entry.getValue());
            default -> true;
        };
    }

    private boolean isValidProfileImage(String value) {
        return value != null && value.startsWith("http");
    }

    private <T> void logUpdatedFields(Class<T> entityClass, Update update) {
        var updatedFields = String.join(", ", update.getUpdateObject().keySet());
        log.info("BEMINDER: {} has been updated: {}", entityClass.getSimpleName(), updatedFields);
    }

    public <T> boolean existsWithReference(Class<T> entityClass, String entityId, String referenceFieldName, String referenceId) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(entityId))
                .and(referenceFieldName + ".$id").is(new ObjectId(referenceId)));
        query.fields().position("program", 1);
//        T application = mongoTemplate.findOne(query, entityClass);
//        log.error("existsWithReference: {}", application);

        return mongoTemplate.exists(query, entityClass);
    }

    public <T> boolean touch(Class<T> entitiyClass, String id, String field) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().currentDate(field);
        var updateResult = mongoTemplate.updateFirst(query, update, entitiyClass);

        return updateResult.wasAcknowledged();
    }

    public boolean isValidKoreanPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^010\\d{8}$");
    }
}
