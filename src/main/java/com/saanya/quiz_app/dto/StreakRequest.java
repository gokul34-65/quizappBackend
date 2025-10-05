package com.saanya.quiz_app.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class StreakRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Streak count is required")
    @Min(value = 0, message = "Streak count must be positive")
    private Integer streakCount;

    private String category;
}
