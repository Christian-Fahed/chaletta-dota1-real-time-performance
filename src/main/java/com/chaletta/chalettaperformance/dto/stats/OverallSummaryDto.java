package com.chaletta.chalettaperformance.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OverallSummaryDto {
    private Long totalMatches;
    private Long totalPlayers;
    private Long totalKills;
    private Long totalDeaths;
    private Long totalAssists;
    private String mostPlayedHero;
    private String topPlayer;
    private String topPlayerBestHero;
}
