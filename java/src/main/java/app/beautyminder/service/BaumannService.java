package app.beautyminder.service;

import app.beautyminder.dto.BaumannTypeDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Service class for calculating the results of the Baumann skin type test.
 * This class handles the logic for interpreting the responses to the test
 * and calculating the individual's skin type based on their responses.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BaumannService {

    // A map to hold the scoring functions for different question types or specific questions.
    private final Map<String, Function<Integer, Double>> scoringRules = createScoringRules();
    private Map<String, Integer> metaData;

    @PostConstruct
    public void runUp() {
        metaData = Map.of(
                "hydrationMax", 44,
                "sensitivityMax", 64,
                "pigmentationMax", 57,
                "elasticityMax", 85,
                "moistureRetentionMax", 100
        );
    }

    /**
     * Calculates the Baumann skin type based on the responses provided.
     * The method processes the scores for various skin properties and
     * compiles them into a comprehensive skin type profile.
     *
     * @param responses a map of question keys and the selected option for each.
     * @return a JSON string representing the individual's Baumann skin type.
     */
    public BaumannTypeDTO calculateResults(Map<String, Integer> responses) {
        // log.info("Responses: {}", responses);
        var dScore = calculateCategoryScore(responses, "A", 11);
        var sScore = calculateCategoryScore(responses, "B", 16);
        var pScore = calculateCategoryScore(responses, "C", 14);
        var wScore = calculateCategoryScore(responses, "D", 21);

        var moistureScore = calculateMoistureScore(responses, new String[]{"A3", "A4", "A8", "A11"});

        var dType = dScore >= 22 ? "O" : "D";
        var sType = sScore >= 32 ? "R" : "S";
        var pType = pScore >= 28.5 ? "P" : "N";
        var wType = wScore >= 42.5 ? "W" : "T";

        var skinType = dType + sType + pType + wType;

        var scores = Map.of(
                "hydration", dScore,
                "sensitivity", sScore,
                "pigmentation", pScore,
                "elasticity", wScore,
                "moistureRetention", moistureScore
        );

        return new BaumannTypeDTO(skinType, scores, metaData);
    }

    /**
     * Creates the scoring rules for the test responses.
     * This method initializes a map with functions designed to calculate
     * the score based on the response to each question.
     *
     * @return a map containing scoring functions.
     */
    private Map<String, Function<Integer, Double>> createScoringRules() {
        Map<String, Function<Integer, Double>> rules = new HashMap<>();

        // Default scoring rule (for most questions)
        Function<Integer, Double> defaultRule = choice -> switch (choice) {
            case 1 -> 1.0;
            case 2 -> 2.0;
            case 3 -> 3.0;
            case 4 -> 4.0;
            case 5 -> 2.5; // special case where the 5th choice only scores 2.5
            default -> 0.0; // or throw an exception for an invalid choice
        };

        // Special scoring rule for 2-choice questions
        Function<Integer, Double> twoChoiceRule = choice -> (choice == 1) ? 0.0 : 5.0;

        // Utility method to add rules for a category
        BiConsumer<String, Integer> addRulesForCategory = (prefix, count) -> {
            for (int i = 1; i <= count; i++) {
                rules.put(prefix + i, defaultRule);
            }
        };

        // Apply default rule to each category
        Map<String, Integer> categoryQuestionCounts = Map.of(
                "A", 11, // category A has 11 questions
                "B", 16, // category B has 16 questions
                "C", 14, // category C has 14 questions
                "D", 21  // category D has 21 questions
        );
        categoryQuestionCounts.forEach(addRulesForCategory);

        // Apply special rules to specific questions (identify by their unique IDs)
        rules.put("C14", twoChoiceRule); // specific question in category C
        rules.put("D21", twoChoiceRule); // specific question in category D

        return rules;
    }

    /**
     * Calculates the score for a specific skin property category.
     * The method aggregates scores from individual questions related
     * to a particular skin property.
     *
     * @param responses    the map of all responses.
     * @param prefix       the category identifier.
     * @param numQuestions the number of questions in the category.
     * @return the aggregate score for the category.
     */
    private double calculateCategoryScore(Map<String, Integer> responses, String prefix, int numQuestions) {
        double score = 0;
        for (int i = 1; i <= numQuestions; i++) {
            String key = prefix + i;
            if (responses.containsKey(key)) {
                int value = responses.get(key);
                // Retrieve the correct scoring rule from the map using the question ID (key)
                Function<Integer, Double> scoringRule = scoringRules.getOrDefault(key, choice -> 0.0); // default to 0 score if no rule is found
                // Apply the scoring rule to get the score for this question
                score += scoringRule.apply(value);
            }
        }
        return score;  // returning the normalized score
    }

    // Method to calculate the moisture score.
    private double calculateMoistureScore(Map<String, Integer> responses, String[] moistureKeys) {
        double score = 0;

        for (String key : moistureKeys) {
            if (responses.containsKey(key)) {
                int value = responses.get(key);
                // Retrieve the correct scoring rule from the map using the question ID (key)
                Function<Integer, Double> scoringRule = scoringRules.getOrDefault(key, choice -> 0.0); // default to 0 score if no rule is found
                // Apply the scoring rule to get the score for this question
                score += scoringRule.apply(value);
            }
        }

        // Calculate the moisture percentage. This assumes a maximum score of 16.
        return (score / 16.0) * 100.0;  // This represents moisture1.
    }
}