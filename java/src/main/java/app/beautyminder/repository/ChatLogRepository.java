package app.beautyminder.repository;

import app.beautyminder.domain.ChatLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChatLogRepository extends MongoRepository<ChatLog, String> {

    Optional<ChatLog> findByRoomName(String roomName);

}