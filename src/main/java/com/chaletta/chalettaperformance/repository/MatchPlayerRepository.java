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
 * <p>Win/Loss logic uses {@code teamNumber} (0 = Sentinel, 1 = Scourge)
 * compared against {@code match.teamWinnerSide} for correctness.
 * {@code teamSide} is a slot number (0–9) and must NOT be used for win/loss.</p>
 */
@Repository
public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, Long> {

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
     * Global kills, deaths, assists across all matches.
     */
    @Query("SELECT SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) FROM MatchPlayer mp")
    List<Object[]> globalKDA();

    /**
     * Most played heroes overall, ordered by play count descending.
     */
    @Query("SELECT mp.heroName, COUNT(mp) as cnt FROM MatchPlayer mp " +
            "GROUP BY mp.heroName ORDER BY cnt DESC")
    List<Object[]> mostPlayedHeroes(Pageable pageable);

    // ─── Per Player — Overall ────────────────────────────────────────────────

    /**
     * Overall stats per player: playerId, username, games, kills, deaths, assists.
     */
    @Query("SELECT mp.player.playerId, mp.player.username, " +
            "COUNT(mp), SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp GROUP BY mp.player.playerId, mp.player.username")
    List<Object[]> overallStatsPerPlayer();

    /**
     * Wins per player overall using teamNumber vs teamWinnerSide.
     * Returns: playerId, winCount
     */
    @Query("SELECT mp.player.playerId, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE mp.teamNumber = m.teamWinnerSide " +
            "GROUP BY mp.player.playerId")
    List<Object[]> winsPerPlayerOverall();

    /**
     * Most played hero per player, ordered by count descending.
     * Returns: playerId, heroName, count
     */
    @Query("SELECT mp.player.playerId, mp.heroName, COUNT(mp) as cnt FROM MatchPlayer mp " +
            "GROUP BY mp.player.playerId, mp.heroName ORDER BY cnt DESC")
    List<Object[]> heroPlayCountPerPlayer();

    // ─── Per Player — Date Range ─────────────────────────────────────────────

    /**
     * Stats per player within a Unix timestamp range.
     * Returns: playerId, username, games, kills, deaths, assists
     */
    @Query("SELECT mp.player.playerId, mp.player.username, " +
            "COUNT(mp), SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player.playerId, mp.player.username")
    List<Object[]> statsPerPlayerInRange(@Param("from") Long from, @Param("to") Long to);

    /**
     * Wins per player within a date range using teamNumber.
     * Returns: playerId, winCount
     */
    @Query("SELECT mp.player.playerId, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "AND mp.teamNumber = m.teamWinnerSide " +
            "GROUP BY mp.player.playerId")
    List<Object[]> winsPerPlayerInRange(@Param("from") Long from, @Param("to") Long to);

    /**
     * Games played per player within a date range.
     * Returns: player, count
     */
    @Query("SELECT mp.player, COUNT(mp) as cnt FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player")
    List<Object[]> countGamesPerPlayer(@Param("from") Long from, @Param("to") Long to);

    // ─── Weekly Title Metrics — Date Range ───────────────────────────────────

    /**
     * Sum of kills per player in range, ordered descending.
     * Returns: player, totalKills
     */
    @Query("SELECT mp.player, SUM(mp.kills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.kills) DESC")
    List<Object[]> sumKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Sum of deaths per player in range, ordered descending.
     */
    @Query("SELECT mp.player, SUM(mp.deaths) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.deaths) DESC")
    List<Object[]> sumDeathsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Sum of assists per player in range, ordered descending.
     */
    @Query("SELECT mp.player, SUM(mp.assists) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.assists) DESC")
    List<Object[]> sumAssistsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Max kills in a single game per player in range, ordered descending.
     */
    @Query("SELECT mp.player, MAX(mp.kills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.kills) DESC")
    List<Object[]> maxKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Max deaths in a single game per player in range, ordered descending.
     */
    @Query("SELECT mp.player, MAX(mp.deaths) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.deaths) DESC")
    List<Object[]> maxDeathsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Max assists in a single game per player in range, ordered descending.
     */
    @Query("SELECT mp.player, MAX(mp.assists) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.assists) DESC")
    List<Object[]> maxAssistsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Games played per player in range, ordered descending.
     */
    @Query("SELECT mp.player, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY COUNT(mp) DESC")
    List<Object[]> gamesPlayedPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Wins per player in range using teamNumber vs teamWinnerSide.
     * Returns: player, winCount
     */
    @Query("SELECT mp.player, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "AND mp.teamNumber = m.teamWinnerSide " +
            "GROUP BY mp.player ORDER BY COUNT(mp) DESC")
    List<Object[]> winsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    /**
     * Losses per player in range using teamNumber vs teamWinnerSide.
     * Returns: player, lossCount
     */
    @Query("SELECT mp.player, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "AND mp.teamNumber != m.teamWinnerSide " +
            "GROUP BY mp.player ORDER BY COUNT(mp) DESC")
    List<Object[]> lossesPerPlayer(@Param("from") Long from, @Param("to") Long to);

    // ─── Hero Stats ──────────────────────────────────────────────────────────

    /**
     * Hero stats across all players.
     * Returns: heroName, heroClass, games, kills, deaths, assists
     */
    @Query("SELECT mp.heroName, mp.heroClass, COUNT(mp), SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp GROUP BY mp.heroName, mp.heroClass ORDER BY COUNT(mp) DESC")
    List<Object[]> heroStatsOverall();

    /**
     * Hero win counts using teamNumber vs teamWinnerSide.
     * Returns: heroName, winCount
     */
    @Query("SELECT mp.heroName, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE mp.teamNumber = m.teamWinnerSide " +
            "GROUP BY mp.heroName")
    List<Object[]> heroWins();

    /**
     * Hero stats per player combination.
     * Returns: heroName, heroClass, playerId, username, kills, deaths, assists, games
     */
    @Query("SELECT mp.heroName, mp.heroClass, mp.player.playerId, mp.player.username, " +
            "SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists), COUNT(mp) " +
            "FROM MatchPlayer mp " +
            "GROUP BY mp.heroName, mp.heroClass, mp.player.playerId, mp.player.username")
    List<Object[]> heroStatsPerPlayer();

    /**
     * Hero stats for a specific player by username, ordered by games played descending.
     * Returns: heroName, heroClass, games, kills, deaths, assists
     */
    @Query("SELECT mp.heroName, mp.heroClass, COUNT(mp), " +
            "SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp " +
            "WHERE LOWER(mp.player.username) = LOWER(:username) " +
            "GROUP BY mp.heroName, mp.heroClass " +
            "ORDER BY COUNT(mp) DESC")
    List<Object[]> heroStatsByPlayer(@Param("username") String username);
}