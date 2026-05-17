package com.chaletta.chalettaperformance.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HomePageDto {
    private OverallSummaryDto summary;
    private List<PlayerOverallStatsDto> leaderboard;
    private List<HeroPlayerStatsDto> heroes;
    private List<WeeklyTitleDto> currentWeekTitles;
    private List<RecentMatchDto> recentMatches;
}
