package com.saanya.quiz_app.service;

import com.saanya.quiz_app.dto.LeaderboardEntry;
import com.saanya.quiz_app.dto.StreakHistory;
import com.saanya.quiz_app.dto.StreakRequest;
import com.saanya.quiz_app.dto.StreakResponse;
import com.saanya.quiz_app.model.Streak;
import com.saanya.quiz_app.model.User;
import com.saanya.quiz_app.repository.StreakRepository;
import com.saanya.quiz_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StreakService {

    @Autowired
    private StreakRepository streakRepository;

    @Autowired
    private UserRepository userRepository;

    public StreakResponse saveStreak(StreakRequest request) {
        // 1. Find user
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = userOpt.get();

        // 2. Create and save streak record
        Streak streak = new Streak();
        streak.setUser(user);
        streak.setStreakCount(request.getStreakCount());
        streak.setCategory(request.getCategory());
        streakRepository.save(streak);

        // 3. Check if it's a new personal record
        boolean isNewRecord = false;
        if (request.getStreakCount() > user.getHighestStreak()) {
            user.setHighestStreak(request.getStreakCount());
            userRepository.save(user);
            isNewRecord = true;
        }

        // 4. Return response
        return new StreakResponse(
                "Streak saved successfully!",
                isNewRecord,
                request.getStreakCount(),
                user.getHighestStreak()
        );
    }

    public List<StreakHistory> getUserStreakHistory(Long userId) {
        List<Streak> streaks = streakRepository.findByUserIdOrderByPlayedAtDesc(userId);
        return streaks.stream()
                .map(streak -> new StreakHistory(
                        streak.getStreakCount(),
                        streak.getCategory(),
                        streak.getPlayedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<LeaderboardEntry> getLeaderboard(int limit) {
        List<User> topUsers = userRepository.findTopUsers();
        return topUsers.stream()
                .limit(limit)
                .map(user -> new LeaderboardEntry(
                        user.getUsername(),
                        user.getHighestStreak()
                ))
                .collect(Collectors.toList());
    }

    public Integer getHighestStreak(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(User::getHighestStreak).orElse(0);
    }
}
