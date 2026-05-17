package com.chaletta.chalettaperformance.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HeroPlayerStatsDto {
    private String heroName;
    private String heroClass;
    private Long timesPlayed;
    private Long totalKills;
    private Long totalDeaths;
    private Long totalAssists;
    private Double winRate;

    // Best player for this hero
    private Long bestPlayerId;
    private String bestPlayerUsername;
    private Long bestPlayerKills;
    private Long bestPlayerDeaths;
    private Long bestPlayerAssists;
    private Long bestPlayerGames;
}
