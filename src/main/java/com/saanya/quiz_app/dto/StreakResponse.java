package com.saanya.quiz_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreakResponse {
    private String message;
    private Boolean isNewRecord;
    private Integer currentStreak;
    private Integer highestStreak;
}
