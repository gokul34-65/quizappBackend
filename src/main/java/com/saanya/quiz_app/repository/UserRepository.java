package com.saanya.quiz_app.repository;

import com.saanya.quiz_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    // Spring auto-generates SQL:
    // SELECT * FROM users WHERE username = ?

    boolean existsByUsername(String username);
    // SELECT EXISTS(SELECT 1 FROM users WHERE username = ?)

    @Query("SELECT u FROM User u ORDER BY u.highestStreak DESC")
    List<User> findTopUsers();
    // Custom query for leaderboard
}
