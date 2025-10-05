package com.saanya.quiz_app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saanya.quiz_app.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {
    
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Autowired
    private WebClient webClient;

    @Autowired
    private ObjectMapper objectMapper;

    public Question generateQuestion(String category) {
        // For testing without API key, return mock questions
        if (apiKey.equals("YOUR_GEMINI_API_KEY")) {
            return generateMockQuestion(category);
        }
        
        // 1. Create prompt for AI
        String prompt = createPrompt(category);

        // 2. Call Gemini API
        String response = callGeminiApi(prompt);

        // 3. Parse JSON response
        return parseGeminiResponse(response, category);
    }

    private String createPrompt(String category) {
        return String.format(
                "Generate a multiple choice quiz question about %s. " +
                        "Make it challenging but fair. " +
                        "Return ONLY valid JSON in this exact format: " +
                        "{\"question\": \"Your question here\", \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"], \"correctIndex\": 0} " +
                        "Where correctIndex is 0-3 indicating which option is correct. " +
                        "Do not include any markdown formatting or additional text.",
                category
        );
    }

    private String callGeminiApi(String prompt) {
        try {
            // Build HTTP request body
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.7,
                            "topK", 40,
                            "topP", 0.95,
                            "maxOutputTokens", 1024
                    )
            );

            // Make POST request to Gemini
            String response = webClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();  // Wait for response

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private Question parseGeminiResponse(String response, String category) {
        try {
            // Gemini returns:
            // { "candidates": [{ "content": { "parts": [{ "text": "..." }] } }] }

            // Extract the actual question JSON from nested structure
            JsonNode root = objectMapper.readTree(response);
            String textContent = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();

            // textContent now contains:
            // {"question": "...", "options": [...], "correctIndex": 0}

            // Clean markdown formatting if present
            textContent = textContent.replace("```json", "").replace("```", "").trim();
            
            // Remove any leading/trailing whitespace and newlines
            textContent = textContent.replaceAll("^\\s*", "").replaceAll("\\s*$", "");

            // Parse into Question object
            JsonNode questionNode = objectMapper.readTree(textContent);

            Question question = new Question();
            question.setQuestion(questionNode.get("question").asText());
            question.setCorrectIndex(questionNode.get("correctIndex").asInt());
            question.setCategory(category);

            // Parse options array
            List<String> options = new ArrayList<>();
            for (JsonNode option : questionNode.get("options")) {
                options.add(option.asText());
            }
            question.setOptions(options);

            return question;
        } catch (Exception e) {
            // If parsing fails, return a default question
            return new Question(
                "Error generating question. Please try again.",
                List.of("Option A", "Option B", "Option C", "Option D"),
                0,
                category
            );
        }
    }

    private Question generateMockQuestion(String category) {
        // Mock questions for testing without API key
        List<String> options = List.of("Option A", "Option B", "Option C", "Option D");
        
        switch (category.toLowerCase()) {
            case "science":
                return new Question(
                    "What is the chemical symbol for water?",
                    List.of("H2O", "CO2", "O2", "N2"),
                    0,
                    category
                );
            case "history":
                return new Question(
                    "In which year did World War II end?",
                    List.of("1944", "1945", "1946", "1947"),
                    1,
                    category
                );
            case "sports":
                return new Question(
                    "How many players are on a basketball team?",
                    List.of("4", "5", "6", "7"),
                    1,
                    category
                );
            case "geography":
                return new Question(
                    "What is the capital of France?",
                    List.of("London", "Berlin", "Paris", "Madrid"),
                    2,
                    category
                );
            case "mathematics":
                return new Question(
                    "What is 15 + 27?",
                    List.of("40", "41", "42", "43"),
                    2,
                    category
                );
            case "entertainment":
                return new Question(
                    "Who directed the movie 'Inception'?",
                    List.of("Steven Spielberg", "Christopher Nolan", "Martin Scorsese", "Quentin Tarantino"),
                    1,
                    category
                );
            case "literature":
                return new Question(
                    "Who wrote 'To Kill a Mockingbird'?",
                    List.of("Mark Twain", "Harper Lee", "Ernest Hemingway", "F. Scott Fitzgerald"),
                    1,
                    category
                );
            case "technical":
                return new Question(
                    "What does CPU stand for?",
                    List.of("Central Processing Unit", "Computer Processing Unit", "Central Program Unit", "Computer Program Unit"),
                    0,
                    category
                );
            default:
                return new Question(
                    "What is the answer to this test question?",
                    options,
                    0,
                    category
                );
        }
    }
}
