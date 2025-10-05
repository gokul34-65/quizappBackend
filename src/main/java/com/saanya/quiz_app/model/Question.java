package com.saanya.quiz_app.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private String question;
    private List<String> options;
    private Integer correctIndex;
    private String category;
}

