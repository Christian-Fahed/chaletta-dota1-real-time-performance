package com.chaletta.chalettaperformance.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerWeeklyStatsDto {
    private Long playerId;
    private String username;
    private Long totalGames;
    private Long totalKills;
    private Long totalDeaths;
    private Long totalAssists;
    private Long totalWins;
    private Long totalLosses;
    private Double winRate;
    private Double kdaRatio;
}
