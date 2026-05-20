package com.chaletta.chalettaperformance.service;

import com.chaletta.chalettaperformance.dto.stats.*;
import com.chaletta.chalettaperformance.model.Player;
import com.chaletta.chalettaperformance.repository.MatchPlayerRepository;
import com.chaletta.chalettaperformance.repository.PlayerRepository;
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
    private final PlayerRepository      playerRepository;

    // ─── Overall Stats ───────────────────────────────────────────────────────

// ─── Overall Stats ───────────────────────────────────────────────────────

    public List<PlayerOverallStatsDto> getOverallStats() {
        List<Object[]> stats      = matchPlayerRepository.overallStatsPerPlayer();
        List<Object[]> wins       = matchPlayerRepository.winsPerPlayerOverall();
        List<Object[]> losses     = matchPlayerRepository.lossesPerPlayerOverall();
        List<Object[]> heroes     = matchPlayerRepository.heroPlayCountPerPlayer();
        List<Object[]> heroWins   = matchPlayerRepository.heroWinsPerPlayer();

        Map<Long, Long>    winsMap     = buildLongMap(wins);
        Map<Long, Long>    lossesMap   = buildLongMap(losses);
        Map<Long, String>  heroMap     = buildBestHeroMap(heroes);
        Map<Long, String>  heroWinsMap = buildBestHeroByWinsMap(heroWins);
        Map<Long, Integer> pointsMap   = buildPointsMap(); // ← computed from ratingChange

        List<PlayerOverallStatsDto> leaderboard = stats.stream().map(row -> {
            Long    playerId     = (Long)   row[0];
            String  username     = (String) row[1];
            Long    games        = (Long)   row[2];
            Long    kills        = (Long)   row[3];
            Long    deaths       = (Long)   row[4];
            Long    assists      = (Long)   row[5];
            Long    creepKills   = row[6] != null ? ((Number) row[6]).longValue() : 0L;
            Long    creepDenies  = row[7] != null ? ((Number) row[7]).longValue() : 0L;
            Long    neutralKills = row[8] != null ? ((Number) row[8]).longValue() : 0L;
            Long    w            = winsMap.getOrDefault(playerId, 0L);
            Long    l            = lossesMap.getOrDefault(playerId, 0L);
            Long    scored       = w + l;
            Double  winRate      = scored > 0 ? round(w * 100.0 / scored) : 0.0;
            Double  kda          = deaths > 0
                    ? round((kills + assists) * 1.0 / deaths)
                    : round((kills + assists) * 1.0);
            String  conf         = games < 5 ? "LOW" : games < 20 ? "MEDIUM" : "HIGH";
            Integer points       = pointsMap.getOrDefault(playerId, 100);

            return new PlayerOverallStatsDto(
                    playerId, username, games, kills, deaths, assists,
                    w, l, winRate, 0.0,
                    round(kills   * 1.0 / Math.max(1, games)),
                    round(deaths  * 1.0 / Math.max(1, games)),
                    round(assists * 1.0 / Math.max(1, games)),
                    kda,
                    heroMap.getOrDefault(playerId, "—"),
                    conf,
                    creepKills, creepDenies, neutralKills,
                    points,
                    heroWinsMap.getOrDefault(playerId, "—")
            );
        }).collect(Collectors.toList());

        double groupAvg = leaderboard.stream()
                .mapToDouble(PlayerOverallStatsDto::getWinRate)
                .average()
                .orElse(50.0);

        final int minGames = 10;
        leaderboard.forEach(p -> {
            double weighted = ((p.getTotalGames() * p.getWinRate()) + (minGames * groupAvg))
                    / (p.getTotalGames() + minGames);
            p.setWeightedWinRate(round(weighted));
        });

        leaderboard.sort(Comparator
                .comparingInt((PlayerOverallStatsDto p) -> p.getPoints() != null ? p.getPoints() : 0)
                .reversed());

        return leaderboard;
    }

// ─── Weekly Stats ────────────────────────────────────────────────────────

    public List<PlayerWeeklyStatsDto> getWeeklyStats(Long from, Long to) {
        List<Object[]> stats  = matchPlayerRepository.statsPerPlayerInRange(from, to);
        List<Object[]> wins   = matchPlayerRepository.winsPerPlayerInRange(from, to);
        List<Object[]> losses = matchPlayerRepository.lossesPerPlayerInRange(from, to);

        Map<Long, Long>    winsMap         = buildLongMap(wins);
        Map<Long, Long>    lossesMap       = buildLongMap(losses);
        Map<Long, Integer> weeklyPointsMap = buildWeeklyPointsMap(from, to); // ← THIS WEEK ONLY

        List<PlayerWeeklyStatsDto> leaderboard = stats.stream().map(row -> {
            Long    playerId     = (Long)   row[0];
            String  username     = (String) row[1];
            Long    games        = (Long)   row[2];
            Long    kills        = (Long)   row[3];
            Long    deaths       = (Long)   row[4];
            Long    assists      = (Long)   row[5];
            Long    creepKills   = row[6] != null ? ((Number) row[6]).longValue() : 0L;
            Long    creepDenies  = row[7] != null ? ((Number) row[7]).longValue() : 0L;
            Long    neutralKills = row[8] != null ? ((Number) row[8]).longValue() : 0L;
            Long    w            = winsMap.getOrDefault(playerId, 0L);
            Long    l            = lossesMap.getOrDefault(playerId, 0L);
            Long    scored       = w + l;
            Double  winRate      = scored > 0 ? round(w * 100.0 / scored) : 0.0;
            Double  kda          = deaths > 0
                    ? round((kills + assists) * 1.0 / deaths)
                    : round((kills + assists) * 1.0);
            String  conf         = games < 3 ? "LOW" : games < 8 ? "MEDIUM" : "HIGH";
            Integer points       = weeklyPointsMap.getOrDefault(playerId, 0); // ← weekly delta

            return new PlayerWeeklyStatsDto(
                    playerId, username, games, kills, deaths, assists,
                    w, l, winRate, 0.0, kda, conf,
                    creepKills, creepDenies, neutralKills,
                    points
            );
        }).collect(Collectors.toList());

        double groupAvg = leaderboard.stream()
                .mapToDouble(PlayerWeeklyStatsDto::getWinRate)
                .average()
                .orElse(50.0);

        final int minGames = 5;
        leaderboard.forEach(p -> {
            double weighted = ((p.getTotalGames() * p.getWinRate()) + (minGames * groupAvg))
                    / (p.getTotalGames() + minGames);
            p.setWeightedWinRate(round(weighted));
        });

        // Sort by weekly points descending
        leaderboard.sort(Comparator
                .comparingInt((PlayerWeeklyStatsDto p) -> p.getPoints() != null ? p.getPoints() : 0)
                .reversed());

        return leaderboard;
    }

    // ─── Hero Stats ──────────────────────────────────────────────────────────

    public List<HeroPlayerStatsDto> getHeroStats() {
        List<Object[]> perPlayer = matchPlayerRepository.heroStatsPerPlayer();
        List<Object[]> heroWins  = matchPlayerRepository.heroWins();

        Map<String, Long> winsMap = new HashMap<>();
        for (Object[] row : heroWins) {
            winsMap.put((String) row[0], ((Number) row[1]).longValue());
        }

        // [0] heroName, [1] heroClass, [2] playerId, [3] username,
        // [4] kills,    [5] deaths,    [6] assists,  [7] games
        Map<String, List<Object[]>> byHero = new LinkedHashMap<>();
        for (Object[] row : perPlayer) {
            byHero.computeIfAbsent((String) row[0], k -> new ArrayList<>()).add(row);
        }

        List<HeroPlayerStatsDto> result = new ArrayList<>();
        for (Map.Entry<String, List<Object[]>> entry : byHero.entrySet()) {
            String         heroName = entry.getKey();
            List<Object[]> rows     = entry.getValue();

            String heroClass  = (String) rows.get(0)[1];
            long totalGames   = rows.stream().mapToLong(r -> ((Number) r[7]).longValue()).sum();
            long totalKills   = rows.stream().mapToLong(r -> ((Number) r[4]).longValue()).sum();
            long totalDeaths  = rows.stream().mapToLong(r -> ((Number) r[5]).longValue()).sum();
            long totalAssists = rows.stream().mapToLong(r -> ((Number) r[6]).longValue()).sum();
            Long heroWin      = winsMap.getOrDefault(heroName, 0L);
            Double winRate    = totalGames >= 3 ? round(heroWin * 100.0 / totalGames) : null;

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
                    null, null, username,
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
     * Build playerId -> win count map from query results.
     */
    private Map<Long, Long> buildLongMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }

    /**
     * Build playerId -> most played hero (3+ games threshold).
     * Falls back to most played regardless if no hero meets threshold.
     */
    private Map<Long, String> buildBestHeroMap(List<Object[]> rows) {
        // rows: [0] playerId, [1] heroName, [2] count
        Map<Long, List<Object[]>> byPlayer = new HashMap<>();
        for (Object[] row : rows) {
            byPlayer.computeIfAbsent((Long) row[0], k -> new ArrayList<>()).add(row);
        }

        Map<Long, String> result = new HashMap<>();
        for (Map.Entry<Long, List<Object[]>> entry : byPlayer.entrySet()) {
            entry.getValue().stream()
                    .filter(r -> ((Number) r[2]).longValue() >= 3)
                    .max(Comparator.comparingLong(r -> ((Number) r[2]).longValue()))
                    .ifPresentOrElse(
                            best -> result.put(entry.getKey(), (String) best[1]),
                            () -> entry.getValue().stream()
                                    .max(Comparator.comparingLong(r -> ((Number) r[2]).longValue()))
                                    .ifPresent(best -> result.put(entry.getKey(), (String) best[1]))
                    );
        }
        return result;
    }

    /**
     * Build playerId -> hero with most wins.
     * Rows come ordered by wins DESC so first occurrence per player = best.
     * [0] playerId, [1] heroName, [2] winCount
     */
    private Map<Long, String> buildBestHeroByWinsMap(List<Object[]> rows) {
        Map<Long, String> result = new HashMap<>();
        for (Object[] row : rows) {
            Long playerId = (Long) row[0];
            if (!result.containsKey(playerId)) {
                result.put(playerId, (String) row[1]);
            }
        }
        return result;
    }

    private Double round(Double val) {
        if (val == null) return 0.0;
        return Math.round(val * 100.0) / 100.0;
    }

    /**
     * Compute points dynamically from ratingChange sum.
     * Points = 100 (start) + SUM(all ratingChanges).
     * This is always accurate regardless of re-syncs.
     */
    private Map<Long, Integer> buildPointsMap() {
        Map<Long, Integer> map = new HashMap<>();
        for (Object[] row : matchPlayerRepository.totalRatingChangePerPlayer()) {
            Long playerId  = (Long) row[0];
            int  ratingSum = row[1] != null ? ((Number) row[1]).intValue() : 0;
            map.put(playerId, 100 + ratingSum); // 100 = starting points
        }
        return map;
    }

    /**
     * Compute points delta for a specific week only.
     * Can be negative (bad week) or positive (good week).
     * Does NOT start from 100 — shows net change this week only.
     */
    private Map<Long, Integer> buildWeeklyPointsMap(long from, long to) {
        Map<Long, Integer> map = new HashMap<>();
        for (Object[] row : matchPlayerRepository.totalRatingChangePerPlayerInRange(from, to)) {
            Long playerId = (Long) row[0];
            int  delta    = row[1] != null ? ((Number) row[1]).intValue() : 0;
            map.put(playerId, delta);
        }
        return map;
    }
}