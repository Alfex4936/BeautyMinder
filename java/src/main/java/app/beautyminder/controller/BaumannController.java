package app.beautyminder.controller;

import app.beautyminder.dto.BaumannSurveyAnswerDTO;
import app.beautyminder.dto.BaumannTypeDTO;
import app.beautyminder.service.BaumannService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/baumann") //
public class BaumannController {

    private final BaumannService baumannService;

    private static final List<String> REQUIRED_KEYS = Arrays.asList(
            "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "A10", "A11",
            "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "B10", "B11", "B12", "B13", "B14", "B15", "B16",
            "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "C10", "C11", "C12", "C13", "C14",
            "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "D10", "D11", "D12", "D13", "D14", "D15", "D16", "D17", "D18", "D19", "D20", "D21"
    );

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

//    @GetMapping("/example")
//    public ResponseEntity<BaumannTypeDTO> testBaumann() throws Exception {
//
//        // JSON parser object to parse read file
//        JSONParser jsonParser = new JSONParser();
//
//        try (FileReader reader = new FileReader("src/main/resources/baumann.json")) {
//            // Read JSON file
//            Object obj = jsonParser.parse(reader);
//
//            JSONObject jsonObject = (JSONObject) obj;
//            JSONObject surveyObject = (JSONObject) jsonObject.get("survey");
//            JSONArray categoriesArray = (JSONArray) surveyObject.get("questions");
//
//            // Create a map to store the responses with the scores from the survey inputs.
//            Map<String, Integer> responses = getTestMap(categoriesArray);
//
//            // Call your service method and get the JSON result
//            BaumannTypeDTO resultJson = baumannService.calculateResults(responses);
//
//            // Return the JSON string in the response entity with the appropriate status code
//            return ResponseEntity.ok(resultJson); // This will set the response body to your JSON string
//
//        } catch (IOException | ParseException e) {
//            e.printStackTrace();
//            // Return an internal server error status (500) if there's an exception
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
//        }
//    }

    @NotNull
    private static Map<String, Integer> getTestMap(JSONArray categoriesArray) {
        Map<String, Integer> responses = new HashMap<>();

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
                    JSONArray optionsArray = (JSONArray) questionObject.get("options");

                    // Generate the key for the response map
                    String key = categoryLetters[i] + (j + 1);

                    // Put the number of options into the map with the generated key
                    if (optionsArray != null) {
                        responses.put(key, optionsArray.size());
                    }
                }
            }
        }
        return responses;
    }
}
