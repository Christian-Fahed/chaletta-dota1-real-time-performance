package com.chaletta.chalettaperformance.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HeroStatsDto {
    private String heroName;
    private String heroClass;
    private Long timesPlayed;
    private Long totalKills;
    private Long totalDeaths;
    private Long totalAssists;
    private Double winRate;
}
