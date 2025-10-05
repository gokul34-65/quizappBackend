package com.saanya.quiz_app.repository;

import com.saanya.quiz_app.model.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreakRepository extends JpaRepository<Streak, Long> {

    List<Streak> findByUserIdOrderByPlayedAtDesc(Long userId);
    // SELECT * FROM streaks WHERE user_id = ? ORDER BY played_at DESC
    // Returns user's game history, newest first

    @Query("SELECT s FROM Streak s WHERE s.user.id = :userId ORDER BY s.streakCount DESC")
    List<Streak> findTopStreaksByUserId(@Param("userId") Long userId);
    // Get user's best games

    @Query("SELECT s FROM Streak s ORDER BY s.streakCount DESC")
    List<Streak> findAllOrderByStreakCountDesc();
    // Get all streaks, highest first (for leaderboard)
}
