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
     * Most played hero per player, ordered by count descending.
     * Returns: playerId, heroName, count
     */
    @Query("SELECT mp.player.playerId, mp.heroName, COUNT(mp) as cnt FROM MatchPlayer mp " +
            "GROUP BY mp.player.playerId, mp.heroName ORDER BY cnt DESC")
    List<Object[]> heroPlayCountPerPlayer();

    // ─── Per Player — Date Range ─────────────────────────────────────────────



    // ─── Hero Stats ──────────────────────────────────────────────────────────


    // Overall stats — only scored matches
    @Query("SELECT mp.player.playerId, mp.player.username, " +
            "COUNT(mp), SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.status = 1 AND mp.playerStatus IN (1, 2) " +
            "GROUP BY mp.player.playerId, mp.player.username")
    List<Object[]> overallStatsPerPlayer();

    // Overall stats in date range — only scored matches
    @Query("SELECT mp.player.playerId, mp.player.username, " +
            "COUNT(mp), SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.status = 1 AND mp.playerStatus IN (1, 2) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player.playerId, mp.player.username")
    List<Object[]> statsPerPlayerInRange(@Param("from") Long from, @Param("to") Long to);

    // Wins overall — scored only
    @Query("SELECT mp.player.playerId, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 AND mp.playerStatus = 1 " +
            "GROUP BY mp.player.playerId")
    List<Object[]> winsPerPlayerOverall();

    // Losses overall — scored only
    @Query("SELECT mp.player.playerId, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 AND mp.playerStatus = 2 " +
            "GROUP BY mp.player.playerId")
    List<Object[]> lossesPerPlayerOverall();

    // Wins in range — scored only
    @Query("SELECT mp.player.playerId, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus = 1 " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player.playerId")
    List<Object[]> winsPerPlayerInRange(@Param("from") Long from, @Param("to") Long to);

    // Losses in range — scored only
    @Query("SELECT mp.player.playerId, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus = 2 " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player.playerId")
    List<Object[]> lossesPerPlayerInRange(@Param("from") Long from, @Param("to") Long to);

    // Wins for weekly titles — scored only
    @Query("SELECT mp.player, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus = 1 " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY COUNT(mp) DESC")
    List<Object[]> winsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    // Losses for weekly titles — scored only
    @Query("SELECT mp.player, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus = 2 " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY COUNT(mp) DESC")
    List<Object[]> lossesPerPlayer(@Param("from") Long from, @Param("to") Long to);

    // Count games played — scored only
    @Query("SELECT mp.player, COUNT(mp) as cnt FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus IN (1, 2) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player")
    List<Object[]> countGamesPerPlayer(@Param("from") Long from, @Param("to") Long to);

    // Games played per player in range — scored only
    @Query("SELECT mp.player, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus IN (1, 2) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY COUNT(mp) DESC")
    List<Object[]> gamesPlayedPerPlayer(@Param("from") Long from, @Param("to") Long to);

    // Hero wins — scored only
    @Query("SELECT mp.heroName, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 AND mp.playerStatus = 1 " +
            "GROUP BY mp.heroName")
    List<Object[]> heroWins();

    // Hero stats overall — scored only
    @Query("SELECT mp.heroName, mp.heroClass, COUNT(mp), SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.status = 1 AND mp.playerStatus IN (1, 2) " +
            "GROUP BY mp.heroName, mp.heroClass ORDER BY COUNT(mp) DESC")
    List<Object[]> heroStatsOverall();

    // Hero stats per player — scored only
    @Query("SELECT mp.heroName, mp.heroClass, mp.player.playerId, mp.player.username, " +
            "SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists), COUNT(mp) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.status = 1 AND mp.playerStatus IN (1, 2) " +
            "GROUP BY mp.heroName, mp.heroClass, mp.player.playerId, mp.player.username")
    List<Object[]> heroStatsPerPlayer();

    // Hero stats by player username — scored only
    @Query("SELECT mp.heroName, mp.heroClass, COUNT(mp), " +
            "SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.status = 1 AND mp.playerStatus IN (1, 2) " +
            "AND LOWER(mp.player.username) = LOWER(:username) " +
            "GROUP BY mp.heroName, mp.heroClass " +
            "ORDER BY COUNT(mp) DESC")
    List<Object[]> heroStatsByPlayer(@Param("username") String username);

    // KDA stats per player — scored only
    @Query("SELECT mp.player, SUM(mp.kills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus IN (1, 2) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.kills) DESC")
    List<Object[]> sumKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, SUM(mp.deaths) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus IN (1, 2) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.deaths) DESC")
    List<Object[]> sumDeathsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, SUM(mp.assists) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus IN (1, 2) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.assists) DESC")
    List<Object[]> sumAssistsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MAX(mp.kills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus IN (1, 2) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.kills) DESC")
    List<Object[]> maxKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MAX(mp.deaths) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus IN (1, 2) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.deaths) DESC")
    List<Object[]> maxDeathsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MAX(mp.assists) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.status = 1 " +
            "AND mp.playerStatus IN (1, 2) " +
            "AND m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.assists) DESC")
    List<Object[]> maxAssistsPerPlayer(@Param("from") Long from, @Param("to") Long to);
}