package com.chaletta.chalettaperformance.service;

import com.chaletta.chalettaperformance.dto.stats.*;
import com.chaletta.chalettaperformance.model.Player;
import com.chaletta.chalettaperformance.repository.MatchPlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final MatchPlayerRepository matchPlayerRepository;

    // ─── Overall Stats ───────────────────────────────────────────────────────

    public List<PlayerOverallStatsDto> getOverallStats() {
        List<Object[]> stats  = matchPlayerRepository.overallStatsPerPlayer();
        List<Object[]> wins   = matchPlayerRepository.winsPerPlayerOverall();
        List<Object[]> heroes = matchPlayerRepository.heroPlayCountPerPlayer();

        Map<Long, Long>   winsMap = buildLongMap(wins);
        Map<Long, String> heroMap = buildBestHeroMap(heroes);

        // Build raw DTOs
        List<PlayerOverallStatsDto> leaderboard = stats.stream().map(row -> {
            Long   playerId = (Long)   row[0];
            String username = (String) row[1];
            Long   games    = (Long)   row[2];
            Long   kills    = (Long)   row[3];
            Long   deaths   = (Long)   row[4];
            Long   assists  = (Long)   row[5];
            Long   w        = winsMap.getOrDefault(playerId, 0L);
            Long   losses   = games - w;
            Double winRate  = games > 0 ? round(w * 100.0 / games) : 0.0;
            Double kda      = deaths > 0
                    ? round((kills + assists) * 1.0 / deaths)
                    : round((kills + assists) * 1.0);
            String conf     = games < 5 ? "LOW" : games < 20 ? "MEDIUM" : "HIGH";

            return new PlayerOverallStatsDto(
                    playerId, username, games, kills, deaths, assists,
                    w, losses, winRate,
                    0.0,
                    round(kills   * 1.0 / games),
                    round(deaths  * 1.0 / games),
                    round(assists * 1.0 / games),
                    kda,
                    heroMap.getOrDefault(playerId, "—"),
                    conf
            );
        }).collect(Collectors.toList());

        // Group average win rate
        double groupAvg = leaderboard.stream()
                .mapToDouble(PlayerOverallStatsDto::getWinRate)
                .average()
                .orElse(50.0);

        // Apply weighted win rate
        final int minGames = 10;
        leaderboard.forEach(p -> {
            double weighted = ((p.getTotalGames() * p.getWinRate()) + (minGames * groupAvg))
                    / (p.getTotalGames() + minGames);
            p.setWeightedWinRate(round(weighted));
        });

        // Sort by weighted win rate descending
        leaderboard.sort(Comparator
                .comparingDouble(PlayerOverallStatsDto::getWeightedWinRate)
                .reversed());

        return leaderboard;
    }

    // ─── Weekly Stats ────────────────────────────────────────────────────────

    public List<PlayerWeeklyStatsDto> getWeeklyStats(Long from, Long to) {
        List<Object[]> stats = matchPlayerRepository.statsPerPlayerInRange(from, to);
        List<Object[]> wins  = matchPlayerRepository.winsPerPlayerInRange(from, to);

        Map<Long, Long> winsMap = buildLongMap(wins);

        List<PlayerWeeklyStatsDto> leaderboard = stats.stream().map(row -> {
            Long   playerId = (Long)   row[0];
            String username = (String) row[1];
            Long   games    = (Long)   row[2];
            Long   kills    = (Long)   row[3];
            Long   deaths   = (Long)   row[4];
            Long   assists  = (Long)   row[5];
            Long   w        = winsMap.getOrDefault(playerId, 0L);
            Long   losses   = games - w;
            Double winRate  = games > 0 ? round(w * 100.0 / games) : 0.0;
            Double kda      = deaths > 0
                    ? round((kills + assists) * 1.0 / deaths)
                    : round((kills + assists) * 1.0);
            String conf     = games < 3 ? "LOW" : games < 8 ? "MEDIUM" : "HIGH";

            return new PlayerWeeklyStatsDto(
                    playerId, username, games, kills, deaths, assists,
                    w, losses, winRate, 0.0, kda, conf
            );
        }).collect(Collectors.toList());

        // Group average win rate
        double groupAvg = leaderboard.stream()
                .mapToDouble(PlayerWeeklyStatsDto::getWinRate)
                .average()
                .orElse(50.0);

        // Apply weighted win rate
        final int minGames = 5;
        leaderboard.forEach(p -> {
            double weighted = ((p.getTotalGames() * p.getWinRate()) + (minGames * groupAvg))
                    / (p.getTotalGames() + minGames);
            p.setWeightedWinRate(round(weighted));
        });

        leaderboard.sort(Comparator
                .comparingDouble(PlayerWeeklyStatsDto::getWeightedWinRate)
                .reversed());

        return leaderboard;
    }

    // ─── Hero Stats ──────────────────────────────────────────────────────────

    public List<HeroPlayerStatsDto> getHeroStats() {
        List<Object[]> perPlayer = matchPlayerRepository.heroStatsPerPlayer();
        List<Object[]> heroWins  = matchPlayerRepository.heroWins();

        // Build wins map per hero
        Map<String, Long> winsMap = new HashMap<>();
        for (Object[] row : heroWins) {
            winsMap.put((String) row[0], (Long) row[1]);
        }

        // Group rows by hero name
        Map<String, List<Object[]>> byHero = new LinkedHashMap<>();
        for (Object[] row : perPlayer) {
            String heroName = (String) row[0];
            byHero.computeIfAbsent(heroName, k -> new ArrayList<>()).add(row);
        }

        // heroStatsPerPlayer returns:
        // [0] heroName, [1] heroClass, [2] playerId, [3] username,
        // [4] kills,    [5] deaths,    [6] assists,  [7] games

        List<HeroPlayerStatsDto> result = new ArrayList<>();

        for (Map.Entry<String, List<Object[]>> entry : byHero.entrySet()) {
            String         heroName = entry.getKey();
            List<Object[]> rows     = entry.getValue();

            String heroClass   = (String) rows.get(0)[1];
            long totalGames    = rows.stream().mapToLong(r -> ((Number) r[7]).longValue()).sum();
            long totalKills    = rows.stream().mapToLong(r -> ((Number) r[4]).longValue()).sum();
            long totalDeaths   = rows.stream().mapToLong(r -> ((Number) r[5]).longValue()).sum();
            long totalAssists  = rows.stream().mapToLong(r -> ((Number) r[6]).longValue()).sum();
            Long heroWin       = winsMap.getOrDefault(heroName, 0L);
            Double winRate     = totalGames > 0 ? round(heroWin * 100.0 / totalGames) : 0.0;

            // Best player = most kills with this hero
            Object[] best = rows.stream()
                    .max(Comparator.comparingLong(r -> ((Number) r[4]).longValue()))
                    .orElse(rows.get(0));

            result.add(new HeroPlayerStatsDto(
                    heroName, heroClass,
                    totalGames, totalKills, totalDeaths, totalAssists, winRate,
                    ((Number) best[2]).longValue(),
                    (String)  best[3],
                    ((Number) best[4]).longValue(),
                    ((Number) best[5]).longValue(),
                    ((Number) best[6]).longValue(),
                    ((Number) best[7]).longValue()
            ));
        }

        return result.stream()
                .sorted(Comparator.comparingLong(HeroPlayerStatsDto::getTimesPlayed).reversed())
                .collect(Collectors.toList());
    }

    // ─── Hero Stats By Player ────────────────────────────────────────────────

    public List<HeroPlayerStatsDto> getHeroStatsByPlayer(String username) {
        // heroStatsByPlayer returns:
        // [0] heroName, [1] heroClass, [2] games, [3] kills, [4] deaths, [5] assists

        return matchPlayerRepository.heroStatsByPlayer(username).stream().map(row -> {
            String heroName  = (String)          row[0];
            String heroClass = (String)          row[1];
            Long   played    = ((Number) row[2]).longValue();
            Long   kills     = ((Number) row[3]).longValue();
            Long   deaths    = ((Number) row[4]).longValue();
            Long   assists   = ((Number) row[5]).longValue();

            return new HeroPlayerStatsDto(
                    heroName, heroClass,
                    played, kills, deaths, assists,
                    0.0,
                    null, username,
                    kills, deaths, assists, played
            );
        }).collect(Collectors.toList());
    }

    // ─── Kills Leaderboard ───────────────────────────────────────────────────

    public List<LeaderboardEntryDto> getKillsLeaderboard() {
        return matchPlayerRepository.sumKillsPerPlayer(0L, Long.MAX_VALUE)
                .stream().map(row -> new LeaderboardEntryDto(
                        ((Player) row[0]).getPlayerId(),
                        ((Player) row[0]).getUsername(),
                        ((Number) row[1]).doubleValue()))
                .collect(Collectors.toList());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Build a map of playerId -> win count from query results.
     */
    private Map<Long, Long> buildLongMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }

    /**
     * Build a map of playerId -> best hero name.
     * Best hero = hero with highest KDA among heroes played 3+ times.
     * Falls back to most played hero if no hero meets the threshold.
     */
    private Map<Long, String> buildBestHeroMap(List<Object[]> rows) {
        // rows: [0] playerId, [1] heroName, [2] count
        Map<Long, List<Object[]>> byPlayer = new HashMap<>();
        for (Object[] row : rows) {
            Long playerId = (Long) row[0];
            byPlayer.computeIfAbsent(playerId, k -> new ArrayList<>()).add(row);
        }

        Map<Long, String> result = new HashMap<>();
        for (Map.Entry<Long, List<Object[]>> entry : byPlayer.entrySet()) {
            // Try to find hero with 3+ games (most played among qualifying)
            entry.getValue().stream()
                    .filter(r -> ((Number) r[2]).longValue() >= 3)
                    .max(Comparator.comparingLong(r -> ((Number) r[2]).longValue()))
                    .ifPresentOrElse(
                            best -> result.put(entry.getKey(), (String) best[1]),
                            // fallback: just take the most played regardless
                            () -> entry.getValue().stream()
                                    .max(Comparator.comparingLong(r -> ((Number) r[2]).longValue()))
                                    .ifPresent(best -> result.put(entry.getKey(), (String) best[1]))
                    );
        }
        return result;
    }

    /**
     * Round a double to 2 decimal places.
     */
    private Double round(Double val) {
        if (val == null) return 0.0;
        return Math.round(val * 100.0) / 100.0;
    }
}