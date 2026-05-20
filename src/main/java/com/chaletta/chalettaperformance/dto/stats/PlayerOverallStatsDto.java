package com.chaletta.chalettaperformance.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerOverallStatsDto {
    private Long   playerId;
    private String username;
    private Long   totalGames;
    private Long   totalKills;
    private Long   totalDeaths;
    private Long   totalAssists;
    private Long   totalWins;
    private Long   totalLosses;
    private Double winRate;
    private Double weightedWinRate;
    private Double avgKills;
    private Double avgDeaths;
    private Double avgAssists;
    private Double kdaRatio;
    private String mostPlayedHero;
    private String confidence;
    // ── New fields ──
    private Long   totalCreepKills;
    private Long   totalCreepDenies;
    private Long   totalNeutralKills;
    private Integer points;
}