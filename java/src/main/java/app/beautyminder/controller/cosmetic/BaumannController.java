package app.beautyminder.controller.cosmetic;

import app.beautyminder.dto.BaumannSurveyAnswerDTO;
import app.beautyminder.dto.BaumannTypeDTO;
import app.beautyminder.service.BaumannService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/baumann")
public class BaumannController {

    private final BaumannService baumannService;

    private static final Set<String> REQUIRED_KEYS = Set.of(
            "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "A10", "A11",
            "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "B10", "B11", "B12", "B13", "B14", "B15", "B16",
            "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "C10", "C11", "C12", "C13", "C14",
            "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "D10", "D11", "D12", "D13", "D14", "D15", "D16", "D17", "D18", "D19", "D20", "D21"
    );

    @Operation(
            summary = "Get Baumann Skin Survey",
            description = "바우만 피부 설문 얻기",
            tags = {"Baumann Operations"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "바우만 피부 설문지 결과", content = @Content(
                            examples = @ExampleObject(name = "Survey JSON",

                                    value = "{ " +
                                            "\"A1\": { " +
                                            "\"question_kr\": \"세안 후 제품을 바르지 않은 상태에서 내 피부는?\"," +
                                            "\"options\": [" +
                                            "{ \"description\": \"매우 거칠고 벗겨지거나 텁수룩하다.\", \"option\": 1 }," +
                                            "{ \"description\": \"피부가 꽉 조이는 느낌을 받는다.\", \"option\": 2 }," +
                                            "{ \"description\": \"빛을 반사하지 않고 수분을 잘 공급한다.\", \"option\": 3 }," +
                                            "{ \"description\": \"밝은 빛의 반사로 피부가 반짝인다.\", \"option\": 4 }" +
                                            "]" +
                                            "}," +
                                            "\"D21\": { " +
                                            "\"question_kr\": \"당신은 65세 이상?\"," +
                                            "\"options\": [" +
                                            "{ \"description\": \"미만이다.\", \"option\": 1 }," +
                                            "{ \"description\": \"이상이다.\", \"option\": 2 }" +
                                            "]" +
                                            "}" +
                                            "}",
                                    summary = "Baumann survey response body"),
                            mediaType = "application/json", schema = @Schema(implementation = Map.class
                    ))),
                    @ApiResponse(responseCode = "400", description = "설문지 파싱 실패")
            }
    )
    @GetMapping("/survey") // Get survey questions.
    public ResponseEntity<?> getBaumannSurvey() {
        // JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("src/main/resources/baumann.json")) {
            // Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONObject jsonObject = (JSONObject) obj;
            JSONObject surveyObject = (JSONObject) jsonObject.get("survey");
            JSONArray categoriesArray = (JSONArray) surveyObject.get("questions");

            // Create a map to store the responses with the scores from the survey inputs.
            @NotNull Map<String, JSONObject> responses = getTestMap(categoriesArray);

            // Return the JSON string in the response entity with the appropriate status code
            return ResponseEntity.ok(responses);

        } catch (IOException | ParseException e) {
            // Return an internal server error status (500) if there's an exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Get Baumann Skin Type",
            description = "바우만 피부 타입 얻기",
            tags = {"Baumann Operations"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "typicalResponses", value = "{ \"responses\": {\"A1\": 4, \"B2\": 5, \"D21\": 2} }", summary = "Example Baumann survey request body"),
                            schema = @Schema(implementation = BaumannSurveyAnswerDTO.class)), description = "Baumann Test Survey"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "바우만 피부 결과", content = @Content(
                            examples = @ExampleObject(name = "BMTI",
                                    value = "{ \"skinType\": \"OSNT\", \"scores\": { \"hydration\": 27.5, \"sensitivity\": 16.0, \"pigmentation\": 19.5, \"elasticity\": 39.0, \"moistureRetention\": 65.625 }, \"metadata\": { \"hydrationMax\": 44, \"sensitivityMax\": 64, \"pigmentationMax\": 57, \"elasticityMax\": 85 } }",
                                    summary = "Example Baumann survey response body"),
                            mediaType = "application/json", schema = @Schema(implementation = BaumannTypeDTO.class
                    ))),
                    @ApiResponse(responseCode = "400", description = "설문지 답변 부족")
            }
    )
    @PostMapping("/test")
    public ResponseEntity<BaumannTypeDTO> getBaumann(@RequestBody BaumannSurveyAnswerDTO baumannSurveyAnswerDTO) {
        Map<String, Integer> responses = baumannSurveyAnswerDTO.getResponses();

        // Validate if all keys are present
        Set<String> keys = responses.keySet();
        List<String> missingKeys = REQUIRED_KEYS.stream()
                .filter(requiredKey -> !keys.contains(requiredKey))
                .toList();

        if (!missingKeys.isEmpty()) {
            // Not all required keys are provided, throw an error
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Missing keys: " + missingKeys
            );
        }

        // If all keys are present
        BaumannTypeDTO resultJson = baumannService.calculateResults(responses);
        return ResponseEntity.ok(resultJson);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private static Map<String, JSONObject> getTestMap(JSONArray categoriesArray) {
        Map<String, JSONObject> responses = new LinkedHashMap<>(); // I need an order.

        // Define category mapping
        String[] categoryLetters = {"A", "B", "C", "D"};

        // Iterate through all categories and populate the responses map
        for (int i = 0; i < categoriesArray.size(); i++) {
            JSONObject categoryObject = (JSONObject) categoriesArray.get(i);
            JSONArray questionsArray = (JSONArray) categoryObject.get("questions");

            // Proceed if there are questions in this category
            if (questionsArray != null) {
                for (int j = 0; j < questionsArray.size(); j++) {
                    JSONObject questionObject = (JSONObject) questionsArray.get(j);

                    // Generate the key for the response map
                    String key = categoryLetters[i] + (j + 1);

                    // Create a new JSONObject to store the question_kr and options
                    JSONObject newQuestionObject = new JSONObject();
                    newQuestionObject.put("question_kr", questionObject.get("question_kr"));
                    newQuestionObject.put("options", questionObject.get("options"));

                    // Put the new JSONObject into the map with the generated key
                    responses.put(key, newQuestionObject);
                }
            }
        }
        return responses;
    }

}
