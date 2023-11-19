package app.beautyminder.service;

import app.beautyminder.domain.BaumannTest;
import app.beautyminder.dto.BaumannTypeDTO;
import app.beautyminder.repository.BaumannTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Service
public class BaumannTestService {
    private final BaumannTestRepository baumannTestRepository;

    public BaumannTest save(BaumannTest baumannTest) {
        return baumannTestRepository.save(baumannTest);
    }

    public Optional<BaumannTest> findById(String id) {
        return baumannTestRepository.findById(id);
    }

    public List<BaumannTest> findByUserId(String userId) {
        return baumannTestRepository.findByUserIdOrderByDateAsc(userId);
    }
}