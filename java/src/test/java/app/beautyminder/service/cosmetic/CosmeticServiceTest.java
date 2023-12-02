package app.beautyminder.service.cosmetic;

import app.beautyminder.domain.Cosmetic;
import app.beautyminder.repository.CosmeticRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"awsBasic", "test"})
class CosmeticServiceTest {

    private CosmeticRepository mockCosmeticRepository;
    private MongoTemplate mockMongoTemplate;
    private CosmeticService cosmeticService;

    private Cosmetic createTestCosmetic() {
        LocalDate expirationDate = LocalDate.now().plusYears(1);
        LocalDate purchasedDate = LocalDate.now().minusDays(30);
        return Cosmetic.builder().name("Test Cosmetic").brand("Test Brand").expirationDate(expirationDate).purchasedDate(purchasedDate).category("Test Category").reviewCount(0).totalRating(0).favCount(0).build();
    }

    @BeforeEach
    void setUp() {
        mockCosmeticRepository = mock(CosmeticRepository.class);
        mockMongoTemplate = mock(MongoTemplate.class);
        cosmeticService = new CosmeticService(mockMongoTemplate, mockCosmeticRepository);
    }

    @Test
    void findByIdTest() {
        String id = "testId";
        Cosmetic cosmetic = createTestCosmetic();
        when(mockCosmeticRepository.findById(id)).thenReturn(Optional.of(cosmetic));

        Optional<Cosmetic> result = cosmeticService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(cosmetic, result.get());
    }

    @Test
    void getAllCosmeticsTest() {
        List<Cosmetic> cosmetics = Arrays.asList(createTestCosmetic(), createTestCosmetic());
        when(mockCosmeticRepository.findAll()).thenReturn(cosmetics);

        List<Cosmetic> result = cosmeticService.getAllCosmetics();

        assertEquals(cosmetics, result);
    }

    @Test
    void getAllCosmeticsInPageTest() {
        Page<Cosmetic> cosmeticPage = mock(Page.class);
        Pageable pageable = mock(Pageable.class);
        when(mockCosmeticRepository.findAll(pageable)).thenReturn(cosmeticPage);

        Page<Cosmetic> result = cosmeticService.getAllCosmeticsInPage(pageable);

        assertEquals(cosmeticPage, result);
    }

    @Test
    void createCosmeticTest() {
        Cosmetic cosmetic = createTestCosmetic();
        when(mockCosmeticRepository.save(cosmetic)).thenReturn(cosmetic);

        Cosmetic result = cosmeticService.createCosmetic(cosmetic);

        assertEquals(cosmetic, result);
    }

    @Test
    void deleteCosmeticTest() {
        String id = "testId";
        when(mockCosmeticRepository.existsById(id)).thenReturn(true);

        boolean result = cosmeticService.deleteCosmetic(id);

        assertTrue(result);
        verify(mockCosmeticRepository).deleteById(id);
    }

    @Test
    void updateCosmeticTest() {
        Cosmetic cosmetic = createTestCosmetic();
        when(mockCosmeticRepository.save(cosmetic)).thenReturn(cosmetic);

        Cosmetic result = cosmeticService.updateCosmetic(cosmetic);

        assertEquals(cosmetic, result);
    }

    @Test
    void getCosmeticByIdTest() {
        String id = "testId";
        Cosmetic cosmetic = createTestCosmetic();
        when(mockCosmeticRepository.findById(id)).thenReturn(Optional.of(cosmetic));

        Cosmetic result = cosmeticService.getCosmeticById(id);

        assertEquals(cosmetic, result);
    }

    @Test
    void findCosmeticsByCategoryTest() {
        String category = "testCategory";
        List<Cosmetic> cosmetics = Arrays.asList(createTestCosmetic(), createTestCosmetic());
        when(mockCosmeticRepository.findByCategory(category)).thenReturn(cosmetics);

        List<Cosmetic> result = cosmeticService.findCosmeticsByCategory(category);

        assertEquals(cosmetics, result);
    }

    @Test
    void findCosmeticsExpiringSoonTest() {
        LocalDate date = LocalDate.now().plusDays(10);
        List<Cosmetic> cosmetics = Arrays.asList(createTestCosmetic(), createTestCosmetic());
        when(mockCosmeticRepository.findExpiringSoon(date)).thenReturn(cosmetics);

        List<Cosmetic> result = cosmeticService.findCosmeticsExpiringSoon(date);

        assertEquals(cosmetics, result);
    }

    @Test
    void findCosmeticsByPurchasedDateTest() {
        LocalDate purchasedDate = LocalDate.now().minusDays(30);
        List<Cosmetic> cosmetics = Arrays.asList(createTestCosmetic(), createTestCosmetic());
        when(mockCosmeticRepository.findByPurchasedDate(purchasedDate)).thenReturn(cosmetics);

        List<Cosmetic> result = cosmeticService.findCosmeticsByPurchasedDate(purchasedDate);

        assertEquals(cosmetics, result);
    }

    @Test
    void findCosmeticsByExpirationDateTest() {
        LocalDate expirationDate = LocalDate.now().plusDays(30);
        List<Cosmetic> cosmetics = Arrays.asList(createTestCosmetic(), createTestCosmetic());
        when(mockCosmeticRepository.findByExpirationDate(expirationDate)).thenReturn(cosmetics);

        List<Cosmetic> result = cosmeticService.findCosmeticsByExpirationDate(expirationDate);

        assertEquals(cosmetics, result);
    }

    @Test
    void saveCosmeticWithDefaultImageTest() {
        Cosmetic cosmetic = createTestCosmetic();
        cosmetic.setImages(new ArrayList<>()); // Ensure images list is empty
        when(mockCosmeticRepository.save(cosmetic)).thenReturn(cosmetic);

        Cosmetic result = cosmeticService.saveCosmetic(cosmetic);

        assertFalse(result.getImages().isEmpty()); // Check if default image is added
        assertEquals(cosmetic, result);
    }

    @Test
    void updateCosmeticByIdTest() {
        String id = "testId";
        Cosmetic cosmeticDetails = createTestCosmetic();
        when(mockCosmeticRepository.existsById(id)).thenReturn(true);
        when(mockCosmeticRepository.save(cosmeticDetails)).thenReturn(cosmeticDetails);

        Cosmetic result = cosmeticService.updateCosmetic(id, cosmeticDetails);

        assertNotNull(result);
        assertEquals(cosmeticDetails, result);
    }

    @Test
    void updateCosmeticByIdNotFoundTest() {
        String id = "testId";
        Cosmetic cosmeticDetails = createTestCosmetic();
        when(mockCosmeticRepository.existsById(id)).thenReturn(false);

        Cosmetic result = cosmeticService.updateCosmetic(id, cosmeticDetails);

        assertNull(result);
    }

    @Test
    void getRandomCosmeticTest() {
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.sample(1));
        List<Cosmetic> cosmetics = Collections.singletonList(createTestCosmetic());

        // Create a dummy raw results object.
        Document rawResults = new Document();

        AggregationResults<Cosmetic> aggregationResults = new AggregationResults<>(cosmetics, rawResults);

        when(mockMongoTemplate.aggregate(any(Aggregation.class), eq(Cosmetic.class), eq(Cosmetic.class))).thenReturn(aggregationResults);

        Cosmetic result = cosmeticService.getRandomCosmetic();

        assertNotNull(result);
    }

}