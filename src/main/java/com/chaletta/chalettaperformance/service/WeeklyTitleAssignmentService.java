package com.chaletta.chalettaperformance.service;

import com.chaletta.chalettaperformance.model.Player;
import com.chaletta.chalettaperformance.model.TitleDefinition;
import com.chaletta.chalettaperformance.model.WeeklyTitle;
import com.chaletta.chalettaperformance.repository.MatchPlayerRepository;
import com.chaletta.chalettaperformance.repository.TitleDefinitionRepository;
import com.chaletta.chalettaperformance.repository.WeeklyTitleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyTitleAssignmentService {

    private final WeeklyTitleRepository     weeklyTitleRepository;
    private final MatchPlayerRepository     matchPlayerRepository;
    private final TitleDefinitionRepository titleDefinitionRepository;

    // ─── Entry points ────────────────────────────────────────────────────────

    /**
     * Called by the scheduler every Sunday midnight.
     * Assigns titles for the week that just ended.
     */
    public void assignWeeklyTitles() {
        assignWeeklyTitlesForWeek(LocalDate.now().minusDays(6));
    }

    /**
     * Manual trigger from admin dashboard.
     * Assigns titles for the week starting on the given date.
     */
    public void assignWeeklyTitlesForWeek(LocalDate weekStart) {
        long from = weekStart.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long to   = weekStart.plusDays(6).atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC);

        log.info("Assigning weekly titles for week starting {}", weekStart);

        List<TitleDefinition> definitions = titleDefinitionRepository.findAll();
        for (TitleDefinition definition : definitions) {
            try {
                assignTitle(definition, weekStart, from, to);
            } catch (Exception e) {
                log.error("Failed to assign title '{}': {}", definition.getTitleName(), e.getMessage());
            }
        }
    }

    // ─── Core assignment logic ────────────────────────────────────────────────

    private void assignTitle(TitleDefinition title, LocalDate weekStart, long from, long to) {
        List<Object[]> results = queryMetric(title.getMetric(), title.getAggregation(), from, to);

        if (results == null || results.isEmpty()) {
            log.info("No data for title '{}'", title.getTitleName());
            return;
        }

        // Build games played map for min_games check
        Map<Long, Long> gamesPlayedMap = buildGamesPlayedMap(from, to);

        // Find the first qualifying player
        for (Object[] row : results) {
            Player player = (Player) row[0];
            Double value  = ((Number) row[1]).doubleValue();
            Long   pid    = player.getPlayerId();

            long gamesPlayed = gamesPlayedMap.getOrDefault(pid, 0L);
            if (gamesPlayed < title.getMinGames()) {
                log.info("Player '{}' has {}/{} games required for title '{}'",
                        player.getUsername(), gamesPlayed, title.getMinGames(), title.getTitleName());
                continue;
            }

            // Skip if already assigned this week
            boolean alreadyAssigned = weeklyTitleRepository
                    .findByPlayer_PlayerIdAndWeekStartAndTitleName(pid, weekStart, title.getTitleName())
                    .isPresent();

            if (alreadyAssigned) {
                log.info("Title '{}' already assigned to '{}' for week {}.",
                        title.getTitleName(), player.getUsername(), weekStart);
                return;
            }

            WeeklyTitle wt = new WeeklyTitle();
            wt.setPlayer(player);
            wt.setWeekStart(weekStart);
            wt.setTitleName(title.getTitleName());
            wt.setValue(value);
            weeklyTitleRepository.save(wt);

            log.info("✓ Assigned '{}' to '{}' (value: {})",
                    title.getTitleName(), player.getUsername(), value);
            return;
        }

        log.info("No qualifying player found for title '{}'", title.getTitleName());
    }

    // ─── Metric routing ──────────────────────────────────────────────────────

    /**
     * Routes metric + aggregation combination to the correct repository query.
     * Key format: metric_aggregation (all lowercase).
     * Example: "kills_sum", "creep_kills_min", "wins_sum"
     */
    private List<Object[]> queryMetric(String metric, String aggregation, long from, long to) {
        String key = metric.toLowerCase() + "_" + aggregation.toLowerCase();
        log.info("Querying metric key: {}", key);

        return switch (key) {

            // ── Kills ──────────────────────────────────────────────────────
            case "kills_sum"          -> matchPlayerRepository.sumKillsPerPlayer(from, to);
            case "kills_max"          -> matchPlayerRepository.maxKillsPerPlayer(from, to);
            case "kills_min"          -> matchPlayerRepository.minKillsPerPlayer(from, to);

            // ── Deaths ────────────────────────────────────────────────────
            case "deaths_sum"         -> matchPlayerRepository.sumDeathsPerPlayer(from, to);
            case "deaths_max"         -> matchPlayerRepository.maxDeathsPerPlayer(from, to);
            case "deaths_min"         -> matchPlayerRepository.minDeathsPerPlayer(from, to);

            // ── Assists ───────────────────────────────────────────────────
            case "assists_sum"        -> matchPlayerRepository.sumAssistsPerPlayer(from, to);
            case "assists_max"        -> matchPlayerRepository.maxAssistsPerPlayer(from, to);
            case "assists_min"        -> matchPlayerRepository.minAssistsPerPlayer(from, to);

            // ── Wins / Losses / Games ─────────────────────────────────────
            case "wins_sum",
                 "wins_max"           -> matchPlayerRepository.winsPerPlayer(from, to);
            case "losses_sum",
                 "losses_max"         -> matchPlayerRepository.lossesPerPlayer(from, to);
            case "games_played_sum",
                 "games_played_max"   -> matchPlayerRepository.gamesPlayedPerPlayer(from, to);

            // ── Creep Kills ───────────────────────────────────────────────
            case "creep_kills_sum"    -> matchPlayerRepository.sumCreepKillsPerPlayer(from, to);
            case "creep_kills_max"    -> matchPlayerRepository.maxCreepKillsPerPlayer(from, to);
            case "creep_kills_min"    -> matchPlayerRepository.minCreepKillsPerPlayer(from, to);

            // ── Creep Denies ──────────────────────────────────────────────
            case "creep_denies_sum"   -> matchPlayerRepository.sumCreepDeniesPerPlayer(from, to);
            case "creep_denies_max"   -> matchPlayerRepository.maxCreepDeniesPerPlayer(from, to);
            case "creep_denies_min"   -> matchPlayerRepository.minCreepDeniesPerPlayer(from, to);

            // ── Neutral Kills ─────────────────────────────────────────────
            case "neutral_kills_sum"  -> matchPlayerRepository.sumNeutralKillsPerPlayer(from, to);
            case "neutral_kills_max"  -> matchPlayerRepository.maxNeutralKillsPerPlayer(from, to);
            case "neutral_kills_min"  -> matchPlayerRepository.minNeutralKillsPerPlayer(from, to);

            default -> {
                log.warn("Unknown metric/aggregation combo: '{}'. Check your title definitions.", key);
                yield null;
            }
        };
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Map<Long, Long> buildGamesPlayedMap(long from, long to) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : matchPlayerRepository.countGamesPerPlayer(from, to)) {
            Player player = (Player) row[0];
            map.put(player.getPlayerId(), ((Number) row[1]).longValue());
        }
        return map;
    }
}