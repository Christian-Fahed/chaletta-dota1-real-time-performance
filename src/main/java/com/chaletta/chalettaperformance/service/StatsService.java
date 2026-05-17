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

    public List<PlayerOverallStatsDto> getOverallStats() {
        List<Object[]> stats = matchPlayerRepository.overallStatsPerPlayer();
        List<Object[]> wins  = matchPlayerRepository.winsPerPlayerOverall();
        List<Object[]> heroes = matchPlayerRepository.heroPlayCountPerPlayer();

        Map<Long, Long> winsMap       = buildLongMap(wins);
        Map<Long, String> heroMap     = buildTopHeroMap(heroes);

        return stats.stream().map(row -> {
                    Long playerId   = (Long) row[0];
                    String username = (String) row[1];
                    Long games      = (Long) row[2];
                    Long kills      = (Long) row[3];
                    Long deaths     = (Long) row[4];
                    Long assists    = (Long) row[5];
                    Long w          = winsMap.getOrDefault(playerId, 0L);
                    Long losses     = games - w;
                    Double winRate  = games > 0 ? (w * 100.0 / games) : 0.0;
                    Double kda      = deaths > 0 ? ((kills + assists) * 1.0 / deaths) : (kills + assists) * 1.0;

                    return new PlayerOverallStatsDto(
                            playerId, username, games, kills, deaths, assists,
                            w, losses, winRate,
                            round(kills * 1.0 / games), round(deaths * 1.0 / games),
                            round(assists * 1.0 / games), round(kda),
                            heroMap.getOrDefault(playerId, "—")
                    );
                }).sorted(Comparator.comparingDouble(PlayerOverallStatsDto::getWinRate).reversed())
                .collect(Collectors.toList());
    }

    public List<PlayerWeeklyStatsDto> getWeeklyStats(Long from, Long to) {
        List<Object[]> stats = matchPlayerRepository.statsPerPlayerInRange(from, to);
        List<Object[]> wins  = matchPlayerRepository.winsPerPlayerInRange(from, to);

        Map<Long, Long> winsMap = buildLongMap(wins);

        return stats.stream().map(row -> {
                    Long playerId   = (Long) row[0];
                    String username = (String) row[1];
                    Long games      = (Long) row[2];
                    Long kills      = (Long) row[3];
                    Long deaths     = (Long) row[4];
                    Long assists    = (Long) row[5];
                    Long w          = winsMap.getOrDefault(playerId, 0L);
                    Long losses     = games - w;
                    Double winRate  = games > 0 ? (w * 100.0 / games) : 0.0;
                    Double kda      = deaths > 0 ? ((kills + assists) * 1.0 / deaths) : (kills + assists) * 1.0;

                    return new PlayerWeeklyStatsDto(
                            playerId, username, games, kills, deaths, assists,
                            w, losses, round(winRate), round(kda)
                    );
                }).sorted(Comparator.comparingDouble(PlayerWeeklyStatsDto::getWinRate).reversed())
                .collect(Collectors.toList());
    }

    public List<HeroPlayerStatsDto> getHeroStats() {
        List<Object[]> perPlayer = matchPlayerRepository.heroStatsPerPlayer();
        List<Object[]> heroWins  = matchPlayerRepository.heroWins();

        // wins per hero
        Map<String, Long> winsMap = new HashMap<>();
        for (Object[] row : heroWins) {
            winsMap.put((String) row[0], (Long) row[1]);
        }

        // group by hero
        Map<String, List<Object[]>> byHero = new LinkedHashMap<>();
        for (Object[] row : perPlayer) {
            String heroName = (String) row[0];
            byHero.computeIfAbsent(heroName, k -> new ArrayList<>()).add(row);
        }

        List<HeroPlayerStatsDto> result = new ArrayList<>();

        for (Map.Entry<String, List<Object[]>> entry : byHero.entrySet()) {
            String heroName = entry.getKey();
            List<Object[]> rows = entry.getValue();

            String heroClass  = (String) rows.get(0)[1];
            long totalGames   = rows.stream().mapToLong(r -> (Long) r[7]).sum();
            long totalKills   = rows.stream().mapToLong(r -> (Long) r[4]).sum();
            long totalDeaths  = rows.stream().mapToLong(r -> (Long) r[5]).sum();
            long totalAssists = rows.stream().mapToLong(r -> (Long) r[6]).sum();
            Long heroWin      = winsMap.getOrDefault(heroName, 0L);
            Double winRate    = totalGames > 0 ? round(heroWin * 100.0 / totalGames) : 0.0;

            // best player = most kills with this hero
            Object[] best = rows.stream()
                    .max(Comparator.comparingLong(r -> (Long) r[4]))
                    .orElse(rows.get(0));

            result.add(new HeroPlayerStatsDto(
                    heroName, heroClass, totalGames, totalKills, totalDeaths, totalAssists, winRate,
                    (Long)   best[2],
                    (String) best[3],
                    (Long)   best[4],
                    (Long)   best[5],
                    (Long)   best[6],
                    (Long)   best[7]
            ));
        }

        return result.stream()
                .sorted(Comparator.comparingLong(HeroPlayerStatsDto::getTimesPlayed).reversed())
                .collect(Collectors.toList());
    }

    public List<LeaderboardEntryDto> getKillsLeaderboard() {
        return matchPlayerRepository.sumKillsPerPlayer(0L, Long.MAX_VALUE)
                .stream().map(row -> new LeaderboardEntryDto(
                        ((Player) row[0]).getPlayerId(),
                        ((Player) row[0]).getUsername(),
                        ((Number) row[1]).doubleValue()))
                .collect(Collectors.toList());
    }

    private Map<Long, Long> buildLongMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }

    private Map<Long, String> buildTopHeroMap(List<Object[]> rows) {
        Map<Long, String> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long playerId = (Long) row[0];
            if (!map.containsKey(playerId)) {
                map.put(playerId, (String) row[1]);
            }
        }
        return map;
    }

    private Double round(Double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    public List<HeroPlayerStatsDto> getHeroStatsByPlayer(String username) {
        List<Object[]> rows = matchPlayerRepository.heroStatsByPlayer(username);

        return rows.stream().map(row -> {
            String heroName  = (String) row[0];
            String heroClass = (String) row[1];
            Long played      = (Long)   row[2];
            Long kills       = (Long)   row[3];
            Long deaths      = (Long)   row[4];
            Long assists     = (Long)   row[5];
            Double winRate   = 0.0;

            return new HeroPlayerStatsDto(
                    heroName, heroClass, played, kills, deaths, assists, winRate,
                    null, username, kills, deaths, assists, played
            );
        }).collect(Collectors.toList());
    }

}
