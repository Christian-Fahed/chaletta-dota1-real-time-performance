package com.chaletta.chalettaperformance.service;

import com.chaletta.chalettaperformance.dto.stats.*;
import com.chaletta.chalettaperformance.repository.MatchPlayerRepository;
import com.chaletta.chalettaperformance.repository.MatchRepository;
import com.chaletta.chalettaperformance.repository.PlayerRepository;
import com.chaletta.chalettaperformance.repository.WeeklyTitleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomePageService {

    private final StatsService          statsService;
    private final MatchRepository       matchRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final PlayerRepository      playerRepository;
    private final WeeklyTitleRepository weeklyTitleRepository;

    public HomePageDto getHomePageData() {
        try {
            // ── Global KDA ────────────────────────────────────────────
            List<Object[]> kdaResult = matchPlayerRepository.globalKDA();
            Object[] kda      = kdaResult.isEmpty() ? new Object[]{0L, 0L, 0L} : kdaResult.get(0);
            Long totalMatches = matchRepository.totalMatches();
            Long totalPlayers = playerRepository.totalPlayers();
            Long totalKills   = kda[0] != null ? ((Number) kda[0]).longValue() : 0L;
            Long totalDeaths  = kda[1] != null ? ((Number) kda[1]).longValue() : 0L;
            Long totalAssists = kda[2] != null ? ((Number) kda[2]).longValue() : 0L;

            // ── Most played hero across ALL players ───────────────────
            String mostPlayedHero = matchPlayerRepository
                    .mostPlayedHeroes(PageRequest.of(0, 1))
                    .stream().findFirst()
                    .map(r -> (String) r[0])
                    .orElse("—");

            // ── Leaderboard sorted by points desc ─────────────────────
            List<PlayerOverallStatsDto> leaderboard = statsService.getOverallStats();

            // ── Top player = highest points with 5+ games ─────────────
            PlayerOverallStatsDto topPlayerDto = leaderboard.stream()
                    .filter(p -> p.getTotalGames() != null && p.getTotalGames() >= 5)
                    .findFirst()
                    .orElse(leaderboard.isEmpty() ? null : leaderboard.get(0));

            String topPlayer = topPlayerDto != null ? topPlayerDto.getUsername() : "—";

            // ── Top player's best hero by WINS ────────────────────────
            String topPlayerBestHero = "—";
            if (topPlayerDto != null) {
                topPlayerBestHero = matchPlayerRepository
                        .heroWinsPerPlayer()
                        .stream()
                        .filter(r -> topPlayerDto.getPlayerId().equals((Long) r[0]))
                        .findFirst()
                        .map(r -> (String) r[1])
                        .orElseGet(() -> topPlayerDto.getBestHeroByWins() != null
                                ? topPlayerDto.getBestHeroByWins()
                                : topPlayerDto.getMostPlayedHero() != null
                                ? topPlayerDto.getMostPlayedHero()
                                : "—");
            }

            // ── Summary DTO — order must match OverallSummaryDto fields ──
            OverallSummaryDto summary = new OverallSummaryDto(
                    totalMatches, totalPlayers,
                    totalKills, totalDeaths, totalAssists,
                    topPlayer,          // ← was wrongly set to mostPlayedHero
                    topPlayerBestHero,  // ← was wrongly set to topPlayer
                    mostPlayedHero);    // ← was wrongly set to topPlayerBestHero

            // ── Hero stats ────────────────────────────────────────────
            List<HeroPlayerStatsDto> heroes = statsService.getHeroStats();

            // ── Current week titles ───────────────────────────────────
            LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
            LocalDate weekEnd   = weekStart.plusDays(6);
            List<WeeklyTitleDto> titles = weeklyTitleRepository
                    .findByWeekStartBetween(weekStart, weekEnd)
                    .stream()
                    .map(t -> new WeeklyTitleDto(
                            t.getTitleName(),
                            t.getPlayer().getUsername(),
                            t.getValue()))
                    .collect(Collectors.toList());

            // ── Recent matches (last 5) ───────────────────────────────
            List<RecentMatchDto> recentMatches = matchRepository
                    .findAll(PageRequest.of(0, 5, Sort.by("startedAt").descending()))
                    .stream()
                    .map(m -> {
                        List<RecentMatchPlayerDto> players = matchPlayerRepository
                                .findByMatch_GameId(m.getGameId())
                                .stream()
                                .map(mp -> new RecentMatchPlayerDto(
                                        mp.getPlayer() != null ? mp.getPlayer().getUsername() : "Unknown",
                                        mp.getHeroName(),
                                        mp.getHeroClass(),
                                        mp.getTeamSide(),
                                        mp.getKills(),
                                        mp.getDeaths(),
                                        mp.getAssists(),
                                        mp.getCreepKills(),
                                        mp.getCreepDenies(),
                                        mp.getNeutralKills(),
                                        mp.getRatingChange()
                                ))
                                .collect(Collectors.toList());
                        return new RecentMatchDto(
                                m.getGameId(), m.getStartedAt(),
                                m.getDuration(), m.getTeamWinnerSide(), players);
                    })
                    .collect(Collectors.toList());

            return new HomePageDto(summary, leaderboard, heroes, titles, recentMatches);

        } catch (Exception e) {
            log.error("HomePageService failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to build home page data: " + e.getMessage(), e);
        }
    }
}