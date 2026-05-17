package com.chaletta.chalettaperformance.repository;

import com.chaletta.chalettaperformance.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

/**
 * <h1>MatchRepository</h1>
 *
 * <p>Match Repository Interface containing all methods
 * for managing matches</p>
 */
public interface MatchRepository extends JpaRepository<Match, Long> {
    Page<Match> findByStartedAtBetween(Long from, Long to, Pageable pageable);
    Page<Match> findAll(Pageable pageable);
    @Query("SELECT COUNT(m) FROM Match m")
    Long totalMatches();
}
