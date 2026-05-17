package com.chaletta.chalettaperformance.repository;

import com.chaletta.chalettaperformance.model.WeeklyTitle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * <h1>WeeklyTitleRepository</h1>
 *
 * <p>Weekly Title Repository Interface containing all methods
 * for managing weekly titles</p>
 */
public interface WeeklyTitleRepository extends JpaRepository<WeeklyTitle, Long> {

    /**
     * Find all weekly titles associated with the specified player id.
     * @param playerId The specified player id.
     * @return List of weekly titles for specified player id.
     */
    List<WeeklyTitle> findByPlayer_PlayerId(Long playerId);

    /**
     * Find all weekly titles based on the specified week start.
     * @param weekStart The specified week.
     * @return List of all weekly titles based on the specified week start.
     */
    List<WeeklyTitle> findByWeekStart(LocalDate weekStart);

    Optional<WeeklyTitle> findByPlayer_PlayerIdAndWeekStartAndTitleName(Long playerId, LocalDate weekStart, String titleName);

    List<WeeklyTitle> findByWeekStartBetween(LocalDate from, LocalDate to);

}
