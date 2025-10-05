package com.saanya.quiz_app.controller;

import com.saanya.quiz_app.dto.LeaderboardEntry;
import com.saanya.quiz_app.dto.StreakHistory;
import com.saanya.quiz_app.dto.StreakRequest;
import com.saanya.quiz_app.dto.StreakResponse;
import com.saanya.quiz_app.service.StreakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/streaks")
@CrossOrigin(origins = "http://localhost:3000")
public class StreakController {

    @Autowired
    private StreakService streakService;

    @PostMapping("/save")
    public ResponseEntity<StreakResponse> saveStreak(@Valid @RequestBody StreakRequest request) {
        try {
            StreakResponse response = streakService.saveStreak(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StreakHistory>> getUserStreakHistory(@PathVariable Long userId) {
        try {
            List<StreakHistory> history = streakService.getUserStreakHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<LeaderboardEntry> leaderboard = streakService.getLeaderboard(limit);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/highest/{userId}")
    public ResponseEntity<Integer> getHighestStreak(@PathVariable Long userId) {
        try {
            Integer highestStreak = streakService.getHighestStreak(userId);
            return ResponseEntity.ok(highestStreak);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
