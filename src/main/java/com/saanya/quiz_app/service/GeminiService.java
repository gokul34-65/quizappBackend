package com.saanya.quiz_app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saanya.quiz_app.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

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
        if (apiKey == null || apiKey.equals("YOUR_GEMINI_API_KEY") || apiKey.isEmpty()) {
            logger.warn("Using mock question - API key not configured");
            return generateMockQuestion(category);
        }

        try {
            logger.info("Generating question for category: {}", category);

            // 1. Create prompt for AI
            String prompt = createPrompt(category);

            // 2. Call Gemini API
            String response = callGeminiApi(prompt);

            // 3. Parse JSON response
            Question question = parseGeminiResponse(response, category);
            logger.info("Successfully generated question for category: {}", category);
            return question;

        } catch (Exception e) {
            logger.error("Error generating question for category {}: {}", category, e.getMessage());
            logger.info("Falling back to mock question");
            // Return a fallback question instead of throwing exception
            return generateMockQuestion(category);
        }
    }

    private String createPrompt(String category) {
        // Prompt optimized for SHORT, concise questions
        return String.format(
                "Generate a SHORT and concise quiz question about %s. " +
                        "Keep the question brief and direct - maximum 15 words. " +
                        "Make it unique and varied. " + "Focus on fundamental aspects" + "and think creative"+
                        "The question should be clear and easy to understand.\n\n" +
                        "Return a JSON object with these fields:\n" +
                        "- question: string (SHORT quiz question, max 15 words)\n" +
                        "- options: array of 4 SHORT strings (brief answer choices)\n" +
                        "- correctIndex: number 0-3 (index of correct answer)\n\n" +
                        "Example format:\n" +
                        "{\"question\":\"What is 2+2?\",\"options\":[\"3\",\"4\",\"5\",\"6\"],\"correctIndex\":1}",
                category
        );
    }

    private String callGeminiApi(String prompt) {
        try {
            // Build HTTP request body optimized for Gemini 2.5 Flash
            // High temperature (0.9) and high top_p (0.95) for maximum variety
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    ),
                    "generationConfig", Map.of(
                            "temperature", 1.5,      // High temperature for creativity and variety
                            "topK", 100,
                            "topP", 0.98,            // High top_p for diverse token selection
                            "maxOutputTokens", 2048,
                            "responseMimeType", "application/json"
                    ),
                    "safetySettings", List.of(
                            Map.of(
                                    "category", "HARM_CATEGORY_HARASSMENT",
                                    "threshold", "BLOCK_ONLY_HIGH"
                            ),
                            Map.of(
                                    "category", "HARM_CATEGORY_HATE_SPEECH",
                                    "threshold", "BLOCK_ONLY_HIGH"
                            ),
                            Map.of(
                                    "category", "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                                    "threshold", "BLOCK_ONLY_HIGH"
                            ),
                            Map.of(
                                    "category", "HARM_CATEGORY_DANGEROUS_CONTENT",
                                    "threshold", "BLOCK_ONLY_HIGH"
                            )
                    )
            );

            logger.debug("Calling Gemini API at: {}", apiUrl);

            // Make POST request to Gemini
            String response = webClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.debug("Received response from Gemini API");
            return response;

        } catch (WebClientResponseException e) {
            logger.error("API Error Response: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API request failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to call Gemini API", e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private Question parseGeminiResponse(String response, String category) {
        try {
            logger.debug("Parsing Gemini API response");

            // Parse the API response structure
            JsonNode root = objectMapper.readTree(response);

            // Check if response has candidates
            if (!root.has("candidates") || root.get("candidates").isEmpty()) {
                logger.error("Response missing or empty 'candidates' field");
                logger.debug("Full response: {}", response);
                throw new RuntimeException("Invalid API response structure - no candidates");
            }

            JsonNode candidates = root.get("candidates");
            JsonNode firstCandidate = candidates.get(0);

            // Check for content filtering
            if (firstCandidate.has("finishReason")) {
                String finishReason = firstCandidate.get("finishReason").asText();
                logger.debug("Finish reason: {}", finishReason);

                // Handle safety/content filtering
                if (finishReason.equals("SAFETY") || finishReason.equals("RECITATION") ||
                        finishReason.equals("OTHER") || finishReason.equals("PROHIBITED_CONTENT")) {
                    logger.warn("Content was filtered by Gemini. Reason: {}", finishReason);
                    throw new RuntimeException("Content filtered by safety settings: " + finishReason);
                }
            }

            // Check if content exists
            if (!firstCandidate.has("content")) {
                logger.error("First candidate missing 'content' field");
                logger.debug("Candidate: {}", firstCandidate.toString());
                throw new RuntimeException("Invalid candidate structure - missing 'content'");
            }

            JsonNode content = firstCandidate.get("content");

            // Check if parts exist and are not empty
            if (!content.has("parts")) {
                logger.error("Content missing 'parts' field");
                logger.debug("Content: {}", content.toString());
                throw new RuntimeException("Invalid content structure - missing 'parts'");
            }

            JsonNode parts = content.get("parts");
            if (parts.isEmpty() || parts.get(0) == null) {
                logger.error("Parts array is empty or null");
                logger.debug("Full response: {}", response);
                throw new RuntimeException("Invalid content structure - empty 'parts'");
            }

            // Check if text exists in the first part
            if (!parts.get(0).has("text")) {
                logger.error("First part missing 'text' field");
                logger.debug("First part: {}", parts.get(0).toString());
                throw new RuntimeException("Invalid part structure - missing 'text'");
            }

            String textContent = parts.get(0).get("text").asText();

            if (textContent == null || textContent.trim().isEmpty()) {
                logger.error("Text content is null or empty");
                throw new RuntimeException("Empty text content in response");
            }

            logger.debug("Extracted text content: {}", textContent);

            // Clean the text content
            textContent = textContent.trim();

            // Remove markdown code blocks if present
            textContent = textContent.replaceAll("```json\\s*", "");
            textContent = textContent.replaceAll("```\\s*", "");
            textContent = textContent.trim();

            // Extract JSON object if there's surrounding text
            int jsonStart = textContent.indexOf('{');
            int jsonEnd = textContent.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                textContent = textContent.substring(jsonStart, jsonEnd + 1);
            } else {
                logger.error("No JSON object found in text content: {}", textContent);
                throw new RuntimeException("Could not find JSON object in response");
            }

            logger.debug("Cleaned JSON text: {}", textContent);

            // Parse the question JSON
            JsonNode questionNode = objectMapper.readTree(textContent);

            // Validate required fields exist
            if (!questionNode.has("question")) {
                throw new RuntimeException("Question JSON missing 'question' field");
            }
            if (!questionNode.has("options")) {
                throw new RuntimeException("Question JSON missing 'options' field");
            }
            if (!questionNode.has("correctIndex")) {
                throw new RuntimeException("Question JSON missing 'correctIndex' field");
            }

            // Build Question object
            Question question = new Question();
            question.setQuestion(questionNode.get("question").asText());
            question.setCorrectIndex(questionNode.get("correctIndex").asInt());
            question.setCategory(category);

            // Parse options array
            List<String> options = new ArrayList<>();
            JsonNode optionsNode = questionNode.get("options");

            if (!optionsNode.isArray()) {
                throw new RuntimeException("Options field is not an array");
            }

            if (optionsNode.size() < 2) {
                throw new RuntimeException("Need at least 2 options");
            }

            for (JsonNode option : optionsNode) {
                options.add(option.asText());
            }
            question.setOptions(options);

            // Validate correctIndex is within bounds
            if (question.getCorrectIndex() < 0 || question.getCorrectIndex() >= options.size()) {
                logger.warn("correctIndex {} is out of bounds for {} options, defaulting to 0",
                        question.getCorrectIndex(), options.size());
                question.setCorrectIndex(0);
            }

            logger.info("Successfully parsed question: {}", question.getQuestion());
            return question;

        } catch (Exception e) {
            logger.error("Error parsing Gemini response: {}", e.getMessage());
            logger.debug("Failed response: {}", response);
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }

    private Question generateMockQuestion(String category) {
        logger.info("Generating mock question for category: {}", category);

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
                        "How many players are on a basketball team on the court?",
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
            case "technology":
            case "technical":
                return new Question(
                        "What does CPU stand for?",
                        List.of("Central Processing Unit", "Computer Processing Unit", "Central Program Unit", "Computer Program Unit"),
                        0,
                        category
                );
            default:
                return new Question(
                        "What is 2 + 2?",
                        List.of("3", "4", "5", "6"),
                        1,
                        category
                );
        }
    }
}