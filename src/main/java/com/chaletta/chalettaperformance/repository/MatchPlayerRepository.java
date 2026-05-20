package com.chaletta.chalettaperformance.repository;

import com.chaletta.chalettaperformance.model.MatchPlayer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * <h1>MatchPlayerRepository</h1>
 *
 * <p>Repository for managing match player entries and aggregating
 * performance statistics across matches.</p>
 *
 * <h2>Stat inclusion rules:</h2>
 * <ul>
 *   <li>status = 1 (won)       → always counts for stats, counted as WIN</li>
 *   <li>status = 2 (lost)      → always counts for stats, counted as LOSS</li>
 *   <li>status = 5 (abandoned) → counts for stats ONLY if game duration > 1200s (20 min), NOT a win or loss</li>
 * </ul>
 *
 * <p>Only scored matches (match.status = 1) are included in any stat query.</p>
 */
@Repository
public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, Long> {

    // ─── Stat Inclusion Condition (used in every stats query) ────────────────
    // (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200))

    // ─── Basic Lookups ───────────────────────────────────────────────────────

    /**
     * Get all match player entries for a given game.
     */
    List<MatchPlayer> findByMatch_GameId(Long gameId);

    /**
     * Get all match player entries for a given player.
     */
    List<MatchPlayer> findByPlayer_PlayerId(Long playerId);

    /**
     * Find a specific match player entry by game and player.
     */
    Optional<MatchPlayer> findByMatch_GameIdAndPlayer_PlayerId(Long gameId, Long playerId);

    // ─── Global Stats ────────────────────────────────────────────────────────

    /**
     * Global kills, deaths, assists across all scored matches.
     * Includes abandoned players if game > 20 minutes.
     */
    @Query("SELECT SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200))")
    List<Object[]> globalKDA();

    // ─── Per Player — Overall ────────────────────────────────────────────────

    /**
     * Overall stats per player across all scored matches.
     * Includes abandoned players if game > 20 minutes.
     * Returns: playerId, username, games, kills, deaths, assists, creep stats
     */
    @Query("SELECT mp.player.playerId, mp.player.username, " +
            "COUNT(mp), SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists), " +
            "SUM(mp.creepKills), SUM(mp.creepDenies), SUM(mp.neutralKills) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "GROUP BY mp.player.playerId, mp.player.username")
    List<Object[]> overallStatsPerPlayer();

    @Query("SELECT mp.heroName, COUNT(mp) as plays FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.heroName IS NOT NULL " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "GROUP BY mp.heroName ORDER BY plays DESC")
    List<Object[]> mostPlayedHeroes(Pageable pageable);

    // Total rating change per player across all games
    @Query("SELECT mp.player.playerId, SUM(mp.ratingChange) FROM MatchPlayer mp " +
            "GROUP BY mp.player.playerId")
    List<Object[]> totalRatingChangePerPlayer();

    // Total rating change per player within a date range (weekly)
    @Query("SELECT mp.player.playerId, SUM(mp.ratingChange) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player.playerId")
    List<Object[]> totalRatingChangePerPlayerInRange(@Param("from") Long from, @Param("to") Long to);

    // Wins per hero per player — for best hero by wins calculation
    @Query("SELECT mp.player.playerId, mp.heroName, COUNT(mp) as wins " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.status = 1 AND mp.playerStatus = 1 " +
            "GROUP BY mp.player.playerId, mp.heroName " +
            "ORDER BY wins DESC")
    List<Object[]> heroWinsPerPlayer();

    /**
     * Wins per player overall.
     * Only status = 1 counts as a win. Abandoned never wins.
     * Returns: playerId, winCount
     */
    @Query("SELECT mp.player.playerId, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 AND mp.playerStatus = 1 " +
            "GROUP BY mp.player.playerId")
    List<Object[]> winsPerPlayerOverall();

    /**
     * Losses per player overall.
     * Only status = 2 counts as a loss. Abandoned never loses.
     * Returns: playerId, lossCount
     */
    @Query("SELECT mp.player.playerId, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 AND mp.playerStatus = 2 " +
            "GROUP BY mp.player.playerId")
    List<Object[]> lossesPerPlayerOverall();

    /**
     * Most played hero per player, ordered by count descending.
     * Includes abandoned players if game > 20 minutes.
     * Returns: playerId, heroName, count
     */
    @Query("SELECT mp.player.playerId, mp.heroName, COUNT(mp) as cnt FROM MatchPlayer mp " +
            "JOIN mp.match m " +
            "WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "GROUP BY mp.player.playerId, mp.heroName ORDER BY cnt DESC")
    List<Object[]> heroPlayCountPerPlayer();

    // ─── Per Player — Date Range ─────────────────────────────────────────────

    /**
     * Stats per player within a Unix timestamp range.
     * Includes abandoned players if game > 20 minutes.
     * Returns: playerId, username, games, kills, deaths, assists, creep stats
     */
    @Query("SELECT mp.player.playerId, mp.player.username, " +
            "COUNT(mp), SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists), " +
            "SUM(mp.creepKills), SUM(mp.creepDenies), SUM(mp.neutralKills) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player.playerId, mp.player.username")
    List<Object[]> statsPerPlayerInRange(@Param("from") Long from, @Param("to") Long to);

    /**
     * Wins per player in date range. Only status = 1.
     * Returns: playerId, winCount
     */
    @Query("SELECT mp.player.playerId, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus = 1 " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player.playerId")
    List<Object[]> winsPerPlayerInRange(@Param("from") Long from, @Param("to") Long to);

    /**
     * Losses per player in date range. Only status = 2.
     * Returns: playerId, lossCount
     */
    @Query("SELECT mp.player.playerId, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus = 2 " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player.playerId")
    List<Object[]> lossesPerPlayerInRange(@Param("from") Long from, @Param("to") Long to);

    /**
     * Count games played per player in date range.
     * Includes abandoned if game > 20 minutes.
     * Returns: player, count
     */
    @Query("SELECT mp.player, COUNT(mp) as cnt FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player")
    List<Object[]> countGamesPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Games played per player in date range, ordered descending.
     * Includes abandoned if game > 20 minutes.
     * Returns: player, count
     */
    @Query("SELECT mp.player, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY COUNT(mp) DESC")
    List<Object[]> gamesPlayedPerPlayer(@Param("from") Long from, @Param("to") Long to);

    // ─── Weekly Title Metrics ─────────────────────────────────────────────────

    /**
     * Wins per player in date range for weekly title assignment.
     * Only status = 1. Returns: player, winCount
     */
    @Query("SELECT mp.player, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus = 1 " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY COUNT(mp) DESC")
    List<Object[]> winsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Losses per player in date range for weekly title assignment.
     * Only status = 2. Returns: player, lossCount
     */
    @Query("SELECT mp.player, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus = 2 " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY COUNT(mp) DESC")
    List<Object[]> lossesPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Sum of kills per player in date range.
     * Includes abandoned if game > 20 minutes.
     */
    @Query("SELECT mp.player, SUM(mp.kills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.kills) DESC")
    List<Object[]> sumKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Sum of deaths per player in date range.
     * Includes abandoned if game > 20 minutes.
     */
    @Query("SELECT mp.player, SUM(mp.deaths) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.deaths) DESC")
    List<Object[]> sumDeathsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Sum of assists per player in date range.
     * Includes abandoned if game > 20 minutes.
     */
    @Query("SELECT mp.player, SUM(mp.assists) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.assists) DESC")
    List<Object[]> sumAssistsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Max kills in a single game per player in date range.
     * Includes abandoned if game > 20 minutes.
     */
    @Query("SELECT mp.player, MAX(mp.kills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.kills) DESC")
    List<Object[]> maxKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Max deaths in a single game per player in date range.
     * Includes abandoned if game > 20 minutes.
     */
    @Query("SELECT mp.player, MAX(mp.deaths) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.deaths) DESC")
    List<Object[]> maxDeathsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Max assists in a single game per player in date range.
     * Includes abandoned if game > 20 minutes.
     */
    @Query("SELECT mp.player, MAX(mp.assists) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.assists) DESC")
    List<Object[]> maxAssistsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    // ─── Hero Stats ──────────────────────────────────────────────────────────

    /**
     * Hero wins overall. Only status = 1 counts as a win.
     * Returns: heroName, winCount
     */
    @Query("SELECT mp.heroName, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 AND mp.playerStatus = 1 " +
            "GROUP BY mp.heroName")
    List<Object[]> heroWins();

    /**
     * Hero stats overall across all players.
     * Includes abandoned if game > 20 minutes.
     * Returns: heroName, heroClass, games, kills, deaths, assists
     */
    @Query("SELECT mp.heroName, mp.heroClass, COUNT(mp), SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "GROUP BY mp.heroName, mp.heroClass ORDER BY COUNT(mp) DESC")
    List<Object[]> heroStatsOverall();

    /**
     * Hero stats per player combination.
     * Includes abandoned if game > 20 minutes.
     * Returns: heroName, heroClass, playerId, username, kills, deaths, assists, games
     */
    @Query("SELECT mp.heroName, mp.heroClass, mp.player.playerId, mp.player.username, " +
            "SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists), COUNT(mp) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "GROUP BY mp.heroName, mp.heroClass, mp.player.playerId, mp.player.username")
    List<Object[]> heroStatsPerPlayer();

    /**
     * Hero stats for a specific player by username, ordered by games played descending.
     * Includes abandoned if game > 20 minutes.
     * Returns: heroName, heroClass, games, kills, deaths, assists
     */
    @Query("SELECT mp.heroName, mp.heroClass, COUNT(mp), " +
            "SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND LOWER(mp.player.username) = LOWER(:username) " +
            "GROUP BY mp.heroName, mp.heroClass " +
            "ORDER BY COUNT(mp) DESC")
    List<Object[]> heroStatsByPlayer(@Param("username") String username);

    // ─── Creep Kills ─────────────────────────────────────────────────────────

    @Query("SELECT mp.player, SUM(mp.creepKills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.creepKills) DESC")
    List<Object[]> sumCreepKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MAX(mp.creepKills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.creepKills) DESC")
    List<Object[]> maxCreepKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MIN(mp.creepKills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MIN(mp.creepKills) ASC")
    List<Object[]> minCreepKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

// ─── Creep Denies ─────────────────────────────────────────────────────────

    @Query("SELECT mp.player, SUM(mp.creepDenies) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.creepDenies) DESC")
    List<Object[]> sumCreepDeniesPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MAX(mp.creepDenies) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.creepDenies) DESC")
    List<Object[]> maxCreepDeniesPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MIN(mp.creepDenies) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MIN(mp.creepDenies) ASC")
    List<Object[]> minCreepDeniesPerPlayer(@Param("from") Long from, @Param("to") Long to);

// ─── Neutral Kills ────────────────────────────────────────────────────────

    @Query("SELECT mp.player, SUM(mp.neutralKills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.neutralKills) DESC")
    List<Object[]> sumNeutralKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MAX(mp.neutralKills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.neutralKills) DESC")
    List<Object[]> maxNeutralKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MIN(mp.neutralKills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MIN(mp.neutralKills) ASC")
    List<Object[]> minNeutralKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MIN(mp.kills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MIN(mp.kills) ASC")
    List<Object[]> minKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MIN(mp.deaths) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MIN(mp.deaths) ASC")
    List<Object[]> minDeathsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MIN(mp.assists) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND (mp.playerStatus IN (1, 2) OR (mp.playerStatus = 5 AND m.duration > 1200)) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MIN(mp.assists) ASC")
    List<Object[]> minAssistsPerPlayer(@Param("from") Long from, @Param("to") Long to);
}