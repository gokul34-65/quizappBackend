package com.saanya.quiz_app.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class QuizRequest {

    @NotBlank(message = "Category is required")
    private String category;
}
