package com.saanya.quiz_app.controller;

import com.saanya.quiz_app.dto.QuizRequest;
import com.saanya.quiz_app.model.Question;
import com.saanya.quiz_app.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "http://localhost:3000")
public class QuizController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/generate")
    public ResponseEntity<Question> generateQuestion(@Valid @RequestBody QuizRequest request) {
        try {
            Question question = geminiService.generateQuestion(request.getCategory());
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
