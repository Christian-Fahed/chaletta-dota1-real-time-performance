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
 * <h1>MatchRepository</h1>
 *
 * <p>Match Player Repository Interface containing all methods
 * for managing match players</p>
 */
@Repository
public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, Long> {

    /**
     * Get all match players associated with the specified game id.
     * @param gameId The specified game id.
     * @return List of match players associated with the game id.
     */
    List<MatchPlayer> findByMatch_GameId(Long gameId);

    /**
     * Get all match players associated with the specified player id.
     * @param playerId The specified player id.
     * @return List of all match players associated with the player id.
     */
    List<MatchPlayer> findByPlayer_PlayerId(Long playerId);

    // Count games played per player in a date range
    @Query("SELECT mp.player, COUNT(mp) as cnt FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player")
    List<Object[]> countGamesPerPlayer(@Param("from") Long from, @Param("to") Long to);

    // Aggregate a numeric stat per player in a date range
    @Query("SELECT mp.player, SUM(mp.kills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.kills) DESC")
    List<Object[]> sumKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, SUM(mp.deaths) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.deaths) DESC")
    List<Object[]> sumDeathsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, SUM(mp.assists) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY SUM(mp.assists) DESC")
    List<Object[]> sumAssistsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MAX(mp.kills) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.kills) DESC")
    List<Object[]> maxKillsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MAX(mp.deaths) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.deaths) DESC")
    List<Object[]> maxDeathsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, MAX(mp.assists) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY MAX(mp.assists) DESC")
    List<Object[]> maxAssistsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    @Query("SELECT mp.player, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player ORDER BY COUNT(mp) DESC")
    List<Object[]> gamesPlayedPerPlayer(@Param("from") Long from, @Param("to") Long to);

    // Losses — player's team side does not match the winner side
    @Query("SELECT mp.player, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "AND mp.teamSide != m.teamWinnerSide " +
            "GROUP BY mp.player ORDER BY COUNT(mp) DESC")
    List<Object[]> lossesPerPlayer(@Param("from") Long from, @Param("to") Long to);

    // Wins — player's team side matches the winner side
    @Query("SELECT mp.player, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "AND mp.teamSide = m.teamWinnerSide " +
            "GROUP BY mp.player ORDER BY COUNT(mp) DESC")
    List<Object[]> winsPerPlayer(@Param("from") Long from, @Param("to") Long to);

    // Overall stats per player
    @Query("SELECT mp.player.playerId, mp.player.username, " +
            "COUNT(mp), SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp GROUP BY mp.player.playerId, mp.player.username")
    List<Object[]> overallStatsPerPlayer();

    // Overall stats per player in date range
    @Query("SELECT mp.player.playerId, mp.player.username, " +
            "COUNT(mp), SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp JOIN mp.match m " +
            "WHERE m.startedAt BETWEEN :from AND :to " +
            "GROUP BY mp.player.playerId, mp.player.username")
    List<Object[]> statsPerPlayerInRange(@Param("from") Long from, @Param("to") Long to);

    // Wins per player in date range
    @Query("SELECT mp.player.playerId, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE m.startedAt BETWEEN :from AND :to " +
            "AND mp.teamSide = m.teamWinnerSide " +
            "GROUP BY mp.player.playerId")
    List<Object[]> winsPerPlayerInRange(@Param("from") Long from, @Param("to") Long to);

    // Wins per player overall
    @Query("SELECT mp.player.playerId, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE mp.teamSide = m.teamWinnerSide " +
            "GROUP BY mp.player.playerId")
    List<Object[]> winsPerPlayerOverall();

    // Most played hero per player
    @Query("SELECT mp.player.playerId, mp.heroName, COUNT(mp) as cnt FROM MatchPlayer mp " +
            "GROUP BY mp.player.playerId, mp.heroName ORDER BY cnt DESC")
    List<Object[]> heroPlayCountPerPlayer();

    // Hero stats overall
    @Query("SELECT mp.heroName, mp.heroClass, COUNT(mp), SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp GROUP BY mp.heroName, mp.heroClass ORDER BY COUNT(mp) DESC")
    List<Object[]> heroStatsOverall();

    // Hero win rate
    @Query("SELECT mp.heroName, COUNT(mp) FROM MatchPlayer mp " +
            "JOIN mp.match m WHERE mp.teamSide = m.teamWinnerSide " +
            "GROUP BY mp.heroName")
    List<Object[]> heroWins();

    @Query("SELECT mp.heroName, mp.heroClass, mp.player.playerId, mp.player.username, " +
            "SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists), COUNT(mp) " +
            "FROM MatchPlayer mp " +
            "GROUP BY mp.heroName, mp.heroClass, mp.player.playerId, mp.player.username")
    List<Object[]> heroStatsPerPlayer();

    @Query("SELECT SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) FROM MatchPlayer mp")
    List<Object[]> globalKDA();

    @Query("SELECT mp.heroName, COUNT(mp) as cnt FROM MatchPlayer mp " +
            "GROUP BY mp.heroName ORDER BY cnt DESC")
    List<Object[]> mostPlayedHeroes(Pageable pageable);

    @Query("SELECT mp.heroName, mp.heroClass, COUNT(mp), " +
            "SUM(mp.kills), SUM(mp.deaths), SUM(mp.assists) " +
            "FROM MatchPlayer mp " +
            "WHERE LOWER(mp.player.username) = LOWER(:username) " +
            "GROUP BY mp.heroName, mp.heroClass " +
            "ORDER BY COUNT(mp) DESC")
    List<Object[]> heroStatsByPlayer(@Param("username") String username);

    // Find by match and player combined
    Optional<MatchPlayer> findByMatch_GameIdAndPlayer_PlayerId(Long gameId, Long playerId);

}
