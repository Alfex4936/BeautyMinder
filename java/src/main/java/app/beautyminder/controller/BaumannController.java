package app.beautyminder.controller;

import app.beautyminder.service.BaumannService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/baumann") //
public class BaumannController {

    private final BaumannService baumannService;

    @GetMapping()
    public ResponseEntity<String> testBaumann() throws Exception {

        // JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("src/main/resources/baumann.json")) {
            // Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONObject jsonObject = (JSONObject) obj;
            JSONObject surveyObject = (JSONObject) jsonObject.get("survey");
            JSONArray categoriesArray = (JSONArray) surveyObject.get("questions");

            // Create a map to store the responses with the scores from the survey inputs.
            Map<String, Integer> responses = getTestMap(categoriesArray);

            // Call your service method and get the JSON result
            String resultJson = baumannService.calculateResults(responses);

            // Return the JSON string in the response entity with the appropriate status code
            return ResponseEntity.ok(resultJson); // This will set the response body to your JSON string

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            // Return an internal server error status (500) if there's an exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

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
