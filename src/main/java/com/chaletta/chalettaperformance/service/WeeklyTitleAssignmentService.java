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

    private final TitleDefinitionService  titleDefinitionService;
    private final WeeklyTitleRepository weeklyTitleRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final TitleDefinitionRepository titleDefinitionRepository;

    public void assignWeeklyTitles() {
        assignWeeklyTitlesForWeek(LocalDate.now().minusDays(6));
    }

    public void assignWeeklyTitlesForWeek(LocalDate weekStart) {
        long from = weekStart.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long to   = weekStart.plusDays(6).atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC);

        log.info("Assigning weekly titles for week starting {}", weekStart);

        List<TitleDefinition> titles = titleDefinitionRepository.findAll();
        for (TitleDefinition title : titles) {
            try {
                assignTitle(title, weekStart, from, to);
            } catch (Exception e) {
                log.error("Failed to assign title {}: {}", title.getTitleName(), e.getMessage());
            }
        }
    }

    private void assignTitle(TitleDefinition title, LocalDate weekStart, long from, long to) {
        List<Object[]> results = queryMetric(title.getMetric(), title.getAggregation(), from, to);

        if (results == null || results.isEmpty())
        {
            log.info("No data for title {}", title.getTitleName());
            return;
        }

        // Count games per player for min_games check
        List<Object[]> gameCounts = matchPlayerRepository.countGamesPerPlayer(from, to);
        Map<Long, Long> gamesPlayedMap = new HashMap<>();
        for (Object[] row : gameCounts)
        {
            Player player = (Player) row[0];
            Long count = (Long) row[1];
            gamesPlayedMap.put(player.getPlayerId(), count);
        }

        // Find the top player who meets min_games
        for (Object[] row : results) {
            Player player = (Player) row[0];
            Double value  = ((Number) row[1]).doubleValue();

            long gamesPlayed = gamesPlayedMap.getOrDefault(player.getPlayerId(), 0L);
            if (gamesPlayed < title.getMinGames()) {
                log.info("Player {} doesn't meet min_games ({}/{}) for title {}",
                        player.getUsername(), gamesPlayed, title.getMinGames(), title.getTitleName());
                continue;
            }

            // Check if already assigned
            boolean alreadyAssigned = weeklyTitleRepository
                    .findByPlayer_PlayerIdAndWeekStartAndTitleName(
                            player.getPlayerId(), weekStart, title.getTitleName())
                    .isPresent();

            if (alreadyAssigned) {
                log.info("Title {} already assigned to {} this week.", title.getTitleName(), player.getUsername());
                return;
            }

            WeeklyTitle wt = new WeeklyTitle();
            wt.setPlayer(player);
            wt.setWeekStart(weekStart);
            wt.setTitleName(title.getTitleName());
            wt.setValue(value);
            weeklyTitleRepository.save(wt);

            log.info("Assigned title '{}' to {} (value: {})", title.getTitleName(), player.getUsername(), value);
            return;
        }
    }


    private List<Object[]> queryMetric(String metric, String aggregation, long from, long to) {
        String key = metric.toLowerCase() + "_" + aggregation.toUpperCase();
        log.info("Querying metric key: {}", key);

        return switch (key) {
            case "kills_SUM"         -> matchPlayerRepository.sumKillsPerPlayer(from, to);
            case "kills_MAX"         -> matchPlayerRepository.maxKillsPerPlayer(from, to);
            case "deaths_SUM"        -> matchPlayerRepository.sumDeathsPerPlayer(from, to);
            case "deaths_MAX"        -> matchPlayerRepository.maxDeathsPerPlayer(from, to);
            case "assists_SUM"       -> matchPlayerRepository.sumAssistsPerPlayer(from, to);
            case "assists_MAX"       -> matchPlayerRepository.maxAssistsPerPlayer(from, to);
            case "games_played_SUM",
                 "games_played_MAX"  -> matchPlayerRepository.gamesPlayedPerPlayer(from, to);
            case "wins_SUM",
                 "wins_MAX"          -> matchPlayerRepository.winsPerPlayer(from, to);
            case "losses_SUM",
                 "losses_MAX"        -> matchPlayerRepository.lossesPerPlayer(from, to);
            default -> {
                log.warn("Unknown metric/aggregation combo: {}", key);
                yield null;
            }
        };
    }

}
